package com.app.infrastructure.cache;

import com.app.infrastructure.cache.exception.CacheException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class JedisCacheTest {
    @Mock
    private JedisPool jedisPool;

    @InjectMocks
    private JedisCache jedisCache;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldPutObjectInCache() {
        Jedis jedisMock = mock(Jedis.class);
        when(jedisPool.getResource()).thenReturn(jedisMock);

        jedisCache.set("foo", "bar");

        verify(jedisMock).set(any(String.class), any(String.class));
    }

    @Test
    public void shouldNotPutObjectInCache() {
        when(jedisPool.getResource()).thenReturn(null);

        assertThrows(CacheException.class, () -> jedisCache.set("foo", "bar"));
    }

    @Test
    public void shouldGetObjectFromCache() {
        Jedis jedisMock = mock(Jedis.class);
        when(jedisPool.getResource()).thenReturn(jedisMock);

        jedisCache.get("foo");

        verify(jedisMock).get(any(String.class));
    }

    @Test
    public void shouldNotGetObjectFromCache() {
        when(jedisPool.getResource()).thenReturn(null);

        assertThrows(CacheException.class, () -> jedisCache.get("foo"));
    }
}
