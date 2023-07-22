package com.xxxx.seckill.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    //实现对象，向redis发送时的序列化，因为key是string类型，value是对象类型，要在网络中传输必须要经过序列化
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
//        序列化key
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        //value序列化 将对象变为json存入redis
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        //hash类型key的序列化
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        //hash类型value
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        //注入连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }


    // @Bean
    // public DefaultRedisScript<Boolean> script() {
    // 	DefaultRedisScript<Boolean> redisScript = new DefaultRedisScript<>();
    // 	//lock.lua脚本位置和application.yml同级目录
    // 	redisScript.setLocation(new ClassPathResource("lock.lua"));
    // 	redisScript.setResultType(Boolean.class);
    // 	return redisScript;
    // }


//    @Bean
//    public DefaultRedisScript<Long> script() {
//        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
//        //放在和application.yml 同层目录下
//        redisScript.setLocation(new ClassPathResource("stock.lua"));
//        redisScript.setResultType(Long.class);
//        return redisScript;
//    }

}
