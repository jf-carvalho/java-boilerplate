package com.app.infrastructure.cache;

import com.app.infrastructure.cache.CacheInterface;
import com.app.infrastructure.cache.exception.CacheException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class JedisCache implements CacheInterface {
    private final JedisPool jedisPool;

    public JedisCache(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    @Override
    public void set(String key, String value) throws CacheException {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(key, value);
        } catch (Exception e) {
            throw new CacheException("Failed trying to put object in cache: " + e.getMessage());
        }
    }

    @Override
    public String get(String key) throws CacheException {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        } catch (Exception e) {
            throw new CacheException("Failed trying to get object from cache: " + e.getMessage());
        }
    }
}
