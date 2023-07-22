package com.xxxx.seckill;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@Slf4j
public class SeckillDemoTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testLock01(){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //等价于redis中的setnx，占位key，如果已经存在，则返回false，如果不存在，则返回true，可以当成锁
        Boolean lock = valueOperations.setIfAbsent("k1", "v1");
//        获取锁;
        if (lock) {
            log.info("获取到锁");
            valueOperations.set("name", "zhangsan");
            String name = (String) valueOperations.get("name");
            //释放锁
            redisTemplate.delete("k1");
        } else {
            log.error("有其他线程正在占用锁");
        }
    }

    @Test
    public void testLock02(){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //等价于redis中的setnx，占位key，如果已经存在，则返回false，如果不存在，则返回true，可以当成锁
        //为了解决死锁，为锁添加过期时间
        Boolean lock = valueOperations.setIfAbsent("k1", "v1",5, TimeUnit.SECONDS);
//        获取锁;
        if (lock) {
            log.info("获取到锁");
            valueOperations.set("name", "zhangsan");
            String name = (String) valueOperations.get("name");
            // 如果获取锁的线程出现了问题，则会造成死锁
//            int i = 1 / 0;
            //释放锁
            redisTemplate.delete("k1");
        } else {
            log.error("有其他线程正在占用锁");
        }
    }

    @Test
    public void testLock03(){
        String value = UUID.randomUUID().toString();

        ValueOperations valueOperations = redisTemplate.opsForValue();
        //等价于redis中的setnx，占位key，如果已经存在，则返回false，如果不存在，则返回true，可以当成锁
        //为了解决死锁，为锁添加过期时间
        Boolean lock = valueOperations.setIfAbsent("k1", "v1",5, TimeUnit.SECONDS);
//        获取锁;
        if (lock) {
            log.info("获取到锁");
            valueOperations.set("name", "zhangsan");
            String name = (String) valueOperations.get("name");
            // 如果获取锁的线程出现了问题，则会造成死锁
            int i = 1 / 0;
            //释放锁
            redisTemplate.delete("k1");
        } else {
            log.error("有其他线程正在占用锁");
        }
    }
}
