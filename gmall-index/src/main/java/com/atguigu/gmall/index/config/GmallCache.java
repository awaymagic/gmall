package com.atguigu.gmall.index.config;

import org.springframework.transaction.TransactionDefinition;

import java.lang.annotation.*;

/**
 * Date:2021/7/10
 * Author:away
 * Description:
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GmallCache {
    /**
     * 自定义缓存前缀
     * @return
     */
    String prefix() default "gmall:";

    /**
     * 缓存过期时间
     * 单位:分钟
     * @return
     */
    int timeout() default 30;

    /**
     * 为了防止缓存雪崩，给缓存时间添加随机值
     * 范围单位:分钟
     * @return
     */
    int random() default 30;

    /**
     * 为了防止缓存击穿，给缓存添加分布式锁
     * 这里指定分布式锁的前缀
     * @return
     */
    String lock() default "lock:";
}
