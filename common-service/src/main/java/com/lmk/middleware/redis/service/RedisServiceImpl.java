package com.lmk.middleware.redis.service;

import com.lmk.middleware.exception.RedisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class RedisServiceImpl implements RedisService {
    @Resource(name = "defaultRedisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    private final RedisSerializer redisKeySerializer = redisTemplate.getKeySerializer();

    private final RedisSerializer redisValueSerializer = redisTemplate.getValueSerializer();

    private final Logger logger = LoggerFactory.getLogger(RedisServiceImpl.class);

    @Override
    public void save(String key, Object value) {
        try {
            if (!StringUtils.isEmpty(key) && value != null) {
                redisTemplate.opsForValue().set(key, value);
            }
        } catch (Exception e) {
            logger.error("cache to redis error,key:{},value:{},exception:{}", key, value, e);
            throw new RedisException("cache to redis error,key:" + key + ",value:" + value);
        }
    }

    @Override
    public <V> V get(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            return (V) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            logger.error("get data from redis cache error,key:{}", key, e);
            throw new RedisException("get data from redis cache error,key:" + key);
        }
    }

    @Override
    public Set<String> getAllKeys() {
        try {
            Set<String> result = new HashSet<>();
            redisTemplate.execute((RedisCallback<Object>) redisConnection -> {
                Cursor<byte[]> cursor = redisConnection.scan(ScanOptions.scanOptions().match("*").build());
                while (cursor.hasNext()) {
                    result.add(new String(cursor.next()));
                }
                return null;
            });
            return result;
        } catch (Exception e) {
            logger.error("get all keys error", e);
            throw new RedisException("get all keys error");
        }
    }

    @Override
    public boolean delete(String key) {
        if (key == null) {
            throw new RedisException("key can not be null while deleting");
        }
        try {
            return Boolean.TRUE.equals(redisTemplate.delete(key));
        } catch (Exception e) {
            logger.error("key delete error,key:{}", key, e);
            throw new RedisException("key delete error,key:" + key);
        }

    }

    @Override
    public void batchSave(Map<String, Object> map) {
        if (map == null || map.size() == 0) {
            throw new RedisException("no keys to batch get");
        }
        try {
            redisTemplate.executePipelined((RedisCallback<Object>) redisConnection -> {
                map.forEach((key, value) -> {
                    redisConnection.set(redisKeySerializer.serialize(key), redisValueSerializer.serialize(value));
                });
                return null;
            });
        } catch (Exception e) {
            logger.error("batch save error,map:{}", map, e);
            throw new RedisException("batch save error");
        }
    }

    @Override
    public List<Object> batchGet(Set<String> keys) {
        if (keys == null || keys.size() == 0) {
            throw new RedisException("no keys to batch get");
        }
        try {
            return redisTemplate.executePipelined((RedisCallback<Object>) redisConnection -> {
                keys.forEach(key -> redisConnection.get(redisKeySerializer.serialize(key)));
                return null;
            }, redisValueSerializer);
        } catch (Exception e) {
            logger.error("batch get error,keys:{}", keys, e);
            throw new RedisException("batch get error,keys:" + keys);
        }
    }

    @Override
    public void batchDelete(Set<String> keys) {
        if (keys == null || keys.size() == 0) {
            throw new RedisException("no keys to batch delete");
        }
        try {
            redisTemplate.executePipelined((RedisCallback<?>) redisConnection -> {
                keys.forEach(key -> redisConnection.del(key.getBytes()));
                return null;
            });
        } catch (Exception e) {
            logger.error("batch delete error,keys:{}", keys, e);
            throw new RedisException("batch delete error,keys:{}" + keys);
        }
    }
}
