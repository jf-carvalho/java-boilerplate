package com.app.infrastructure.cache;

import com.app.infrastructure.cache.exception.CacheException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Set;

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

    @Override
    public boolean add(String key, String value) throws CacheException {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.sadd(key, value);
        } catch (Exception e) {
            throw new CacheException("Failed trying to add object to cache: " + e.getMessage());
        }

        return true;
    }

    @Override
    public Set<String> getList(String key) throws CacheException {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.smembers(key);
        } catch (Exception e) {
            throw new CacheException("Failed trying to get list members from cache: " + e.getMessage());
        }
    }
}
