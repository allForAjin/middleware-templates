package com.lmk.middleware.redis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {
    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private Integer port;

    @Value("${spring.redis.password}")
    private String password;

    @Value("${spring.redis.timeout}")
    private Long timeout;

    @Value("${spring.redis.custom-database}")
    private Integer customDatabase;

    /**
     * 使用默认 application.yml 中的配置
     *
     * @param connectionFactory 自动注入，redis连接工厂
     * @return defaultRedisTemplate
     */
    @Bean("defaultRedisTemplate")
    public RedisTemplate<String, Object> historyRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = this.getRedisTemplate();
        redisTemplate.setConnectionFactory(connectionFactory);
        return redisTemplate;
    }

    /**
     * 使用自定义配置的 RedisTemplate
     * @return customRedisTemplate
     */
    @Bean("customRedisTemplate")
    public RedisTemplate<String,Object> historyRedisTemplate(){
        RedisTemplate<String,Object> redisTemplate = this.getRedisTemplate();

        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setDatabase(customDatabase);
        redisStandaloneConfiguration.setHostName(host);
        redisStandaloneConfiguration.setPort(port);
        redisStandaloneConfiguration.setPassword(RedisPassword.of(password));

        LettuceConnectionFactory factory = new LettuceConnectionFactory(redisStandaloneConfiguration, LettuceClientConfiguration.builder()
                // 超时时间
                .commandTimeout(Duration.ofMillis(timeout)).build());
        factory.afterPropertiesSet();
        redisTemplate.setConnectionFactory(factory);

        return redisTemplate;
    }

    /**
     * redis key 序列化器
     *
     * @return redisKeySerializer
     */
    @Bean("redisKeySerializer")
    public RedisSerializer<String> redisKeySerializer() {
        return new StringRedisSerializer();
    }

    /**
     * redis value 序列化器
     *
     * @return redisValueSerializer
     */
    @Bean("redisValueSerializer")
    public RedisSerializer<Object> redisValueSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }

    /**
     * 创建RedisTemplate对象并设置序列化器
     *
     * @return RedisTemplate<String, Object>
     */
    private RedisTemplate<String, Object> getRedisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        redisTemplate.setKeySerializer(redisKeySerializer());
        redisTemplate.setHashKeySerializer(redisValueSerializer());

        redisTemplate.setValueSerializer(redisValueSerializer());
        redisTemplate.setHashValueSerializer(redisValueSerializer());

        return redisTemplate;
    }
}
