package com.app.infrastructure.cache;

import com.app.infrastructure.cache.exception.CacheException;

import java.util.Set;

public interface CacheInterface {
    void set(String key, String value) throws CacheException;
    String get(String key) throws CacheException;
    boolean add(String key, String value) throws CacheException;

    Set<String> getList(String key) throws CacheException;
}
