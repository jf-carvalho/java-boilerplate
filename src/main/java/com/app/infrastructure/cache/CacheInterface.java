package com.app.infrastructure.cache;

import com.app.infrastructure.cache.exception.CacheException;

public interface CacheInterface {
    void set(String key, String value) throws CacheException;
    String get(String key) throws CacheException;
    boolean add(String key, String value) throws CacheException;
}
