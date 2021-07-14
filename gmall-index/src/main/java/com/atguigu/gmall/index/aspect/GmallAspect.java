package com.atguigu.gmall.index.aspect;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.index.config.GmallCache;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Date:2021/7/10
 * Author:away
 * Description:
 */
@Aspect
@Component
public class GmallAspect {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RBloomFilter filter;
/*    @Pointcut("execution(* com.atguigu.gmall.index.service.*.*(..))")
    public void pointcut() {
    }
*/

    /**
     * 切点表达式：
     * 第一个*：代表返回值为任意类型
     * 第二个*：代表是任意类
     * 第三个*：代表类中的任意方法
     * ..：代表任意参数
     * <p>
     * 获取目标信息：
     * 目标类：joinPoint.getTarget().getClass()
     * 目标方法签名：(MethodSignature) joinPoint.getSignature()
     * 目标方法：signature.getMethod()
     * 目标方法参数列表：joinPoint.getArgs()
     */
   /* @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        System.out.println("这是前置方法。。。。。。:" + joinPoint.getTarget().getClass().getName());
        System.out.println("这是目标方法:" + signature.getMethod().getName());
        System.out.println("这是目标方法参数列表:" + joinPoint.getArgs().toString());


    }

    @AfterReturning(value = "pointcut()", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Object result) {
        System.out.println("这是返回后通知：");
        ((List<CategoryEntity>) result).forEach(System.out::println);
    }

    @AfterThrowing(value = "pointcut()", throwing = "ex")
    public void afterThrowing(JoinPoint joinPoint, Exception ex) {
        System.out.println(" 异常通知  方法名是: "
                + joinPoint.getSignature().getName() + "  , 抛的异常是: " + ex);
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("前增强");
        Object result = joinPoint.proceed(joinPoint.getArgs());
        System.out.println("后增强");
        return result;
    }*/
    @Around("@annotation(com.atguigu.gmall.index.config.GmallCache)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 获取目标方法
        Method method = signature.getMethod();
        // 获取目标方法上的GmallCache注解
        GmallCache gmallCache = method.getAnnotation(GmallCache.class);
        // 获取目标方法的返回值类型
        Class returnType = signature.getReturnType();

        // 获取缓存注解的前缀
        String prefix = gmallCache.prefix();
        // 获取目标方法的参数列表
        // List<Object> args = Arrays.asList(joinPoint.getArgs());
        List<Object> arr = Arrays.asList(joinPoint.getArgs());
        //System.out.println(StringUtils.strip(args.toString(),"[]"));
        String args = StringUtils.strip(arr.toString(), "[]");

        // 组装缓存的key
        String key = prefix + args;
        // 解决缓存穿透 使用bloom过滤器
        if (!this.filter.contains(key)) {
            return null;
        }

        // 1查询缓存 如果缓存直接命中 返回
        String json = this.redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(json)) {
            // 把json反序列化为CategoryEntity
            return JSON.parseObject(json, returnType);
        }

        // 2缓存中没有，防止缓存击穿：加分布式锁
        RLock fairLock = this.redissonClient.getFairLock(gmallCache.lock() + args);
        fairLock.lock();

        try {
            // 3再次查询缓存，因为在获取分布式锁过程中，可能有其他的请求把数据放入缓存
            String json2 = this.redisTemplate.opsForValue().get(key);
            if (StringUtils.isNotBlank(json2)) {
                return JSON.parseObject(json2, returnType);
            }
            // 4缓存没有 则查询数据库
            Object result = joinPoint.proceed(joinPoint.getArgs());

            // 5查询数据库返回值并放入缓存(缓存穿透 设置短时间null 布隆过滤,  缓存雪崩 设置随机值)
            if (result != null) {
                int timeout = gmallCache.timeout() + new Random().nextInt(gmallCache.random());
                this.redisTemplate.opsForValue().set(key, JSON.toJSONString(result), timeout, TimeUnit.MINUTES);
            }
            return result;
        } finally {
            fairLock.unlock();
        }
    }
}
