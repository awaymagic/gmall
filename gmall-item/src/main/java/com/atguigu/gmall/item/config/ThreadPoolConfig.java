package com.atguigu.gmall.item.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Date:2021/7/13
 * Author:away
 * Description: 线程池配置
 */
@Configuration
public class ThreadPoolConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor(
            @Value("${thread.pool.coreSize}")Integer coreSize,
            @Value("${thread.pool.maxSize}")Integer maxSize,
            @Value("${thread.pool.coreSize}")Integer keepAlive,
            @Value("${thread.pool.coreSize}")Integer blockingSize
    ) {
        return new ThreadPoolExecutor(coreSize, maxSize, keepAlive, TimeUnit.SECONDS, new ArrayBlockingQueue<>(blockingSize));
    }
}
