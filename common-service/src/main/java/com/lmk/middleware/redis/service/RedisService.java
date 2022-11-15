package com.lmk.middleware.redis.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RedisService {
    void save(String key,Object value);

    <V> V get(String key);

    Set<String> getAllKeys();

    boolean delete(String key);

    void batchSave(Map<String,Object> map);

    List<Object> batchGet(Set<String> keys);

    void batchDelete(Set<String> keys);
}
