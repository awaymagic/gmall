package com.atguigu.gmall.index;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;



/**
 * StringRedisTemplate
 * 手动序列化
 *
 */
@SpringBootTest
class GmallIndexApplicationTests {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void contextLoads() {
        this.redisTemplate.opsForValue().set("test2", "away");
        System.out.println(this.redisTemplate.opsForValue().get("test2"));
    }

}
