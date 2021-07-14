package com.atguigu.gmall.index.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.config.GmallCache;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.index.utils.DistributedLock;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Date:2021/7/7
 * Author:away
 * Description:
 */
@Service
public class IndexService {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private DistributedLock lock;

    @Autowired
    private RedissonClient redissonClient;

    // redis标准命名格式用:隔开
    private static final String KEY_PREFIX = "index:cates:";
    private static final String LOCK_PREFIX = "index:cates:lock:";

    public List<CategoryEntity> queryLv1Categories() {
        ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryCategoryByPid(0l);
        List<CategoryEntity> categoryEntities = listResponseVo.getData();
        return categoryEntities;
    }

    @GmallCache(prefix = KEY_PREFIX,timeout = 259200,random = 14400 ,lock = LOCK_PREFIX)
    public List<CategoryEntity> queryLvl2WithSubsByPid(Long pid) {
        ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryLvl2WithSubsByPid(pid);
        return listResponseVo.getData();
    }

    public List<CategoryEntity> queryLvl2WithSubsByPid2(Long pid) {
        // 先查询缓存，如果缓存不为空，则直接返回
        String json = this.redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if (StringUtils.isNotBlank(json)) {
            // 把json反序列化为CategoryEntity
            return JSON.parseArray(json, CategoryEntity.class);
        }
        // 为了防止缓存击穿添加分布式锁
        RLock fairLock = this.redissonClient.getFairLock(LOCK_PREFIX + pid);
        fairLock.lock();

        try {
            // 在当前请求获取锁的过程中，可能已经有其他线程获取到锁，并把数据放入缓存，此时最好再次确认缓存中是否已有
            String json2 = this.redisTemplate.opsForValue().get(KEY_PREFIX + pid);
            if (StringUtils.isNotBlank(json2)) {
                return JSON.parseArray(json2, CategoryEntity.class);
            }
            // 如果缓存为空，则远程调用或者直接查数据库，并放入缓存
            ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryLvl2WithSubsByPid(pid);
            List<CategoryEntity> categoryEntities = listResponseVo.getData();
            // 放入缓存
            if (CollectionUtils.isEmpty(categoryEntities)) {
                // 防止缓存穿透(即数据库没有该数据，也没有该数据缓存，防止并发访问导致mySQL挂掉) 空数据也设置缓存(时间较短).终极方案：布隆过滤器
                this.redisTemplate.opsForValue().set(KEY_PREFIX + pid, JSON.toJSONString(categoryEntities), 5, TimeUnit.MINUTES);
            } else {
                // 防止缓存雪崩，给缓存时间设置随机值Random
                this.redisTemplate.opsForValue().set(KEY_PREFIX + pid, JSON.toJSONString(categoryEntities), 90 + new Random().nextInt(10), TimeUnit.DAYS);
            }
            return categoryEntities;
        } finally {
            fairLock.unlock();
        }
    }

    public void testLock() {

        RLock lock = this.redissonClient.getLock("lock");
        lock.lock();
        try {
            // 获取锁成功的线程，执行业务操作
            String numString = this.redisTemplate.opsForValue().get("num");
            if (StringUtils.isBlank(numString)) {
                this.redisTemplate.opsForValue().set("num", "1");
            }

            int num = Integer.parseInt(numString);
            this.redisTemplate.opsForValue().set("num", String.valueOf(++num));

//            try {
//                TimeUnit.SECONDS.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        } finally {
            lock.unlock();
        }
    }



    public void testLock3() {

        String uuid = UUID.randomUUID().toString();
        Boolean flag = this.lock.lock("lock", uuid, 30);
        if (flag){
            // 获取锁成功的线程，执行业务逻辑
            String numString = this.redisTemplate.opsForValue().get("num");
            if (StringUtils.isBlank(numString)) {
                this.redisTemplate.opsForValue().set("num", "1");
            }
            int num = Integer.parseInt(numString);
            this.redisTemplate.opsForValue().set("num", String.valueOf(++num));

            //this.testSub("look", uuid);
            try {
                TimeUnit.SECONDS.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lock.unlock("look", uuid);
        }
    }

    public void testSub(String lockName, String uuid) {
        lock.lock(lockName, uuid, 30);
        System.out.println("测试可重入锁");
        lock.unlock(lockName, uuid);
    }

    /**
     * 测试基于redis实现分布式锁
     * 1.加锁: setnx
     * 2.释放锁: del
     * 3.重试
     */
/*    public void testLock2() {
        // 获取锁:setnx   设置过期时间，防止在这一步宕机而死锁
        String uuid = UUID.randomUUID().toString();
        Boolean flag = this.redisTemplate.opsForValue().setIfAbsent("lock", uuid,3,TimeUnit.SECONDS);
        if (!flag) {
            try {
                // 获取锁失败线程，进行重试
                Thread.sleep(100);
                testLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            // 获取锁成功的线程，执行业务逻辑
            String numString = this.redisTemplate.opsForValue().get("num");
            if (StringUtils.isBlank(numString)) {
                this.redisTemplate.opsForValue().set("num", "1");
            }
            int num = Integer.parseInt(numString);
            this.redisTemplate.opsForValue().set("num", String.valueOf(++num));
            // 释放锁:判断是否为自己的锁(lua脚本实现:判断和删除的原子性)
            String script = "if(redis.call('get',KEYS[1]) == ARGV[1]) then return redis.call('del',KEYS[1]) else return 0 end";
            this.redisTemplate.execute(new DefaultRedisScript<>(script,Boolean.class), Arrays.asList("lock"), uuid);
//            if (StringUtils.equals(uuid, this.redisTemplate.opsForValue().get("lock"))) {
//                this.redisTemplate.delete("lock");
//            }
        }
    }*/
}
