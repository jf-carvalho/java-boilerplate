package com.app.config;

import com.app.application.service.UserService;
import com.app.infrastructure.cache.CacheInterface;
import com.app.infrastructure.cache.JedisCache;
import com.app.infrastructure.persistence.entity.User;
import com.app.infrastructure.persistence.repository.RepositoryInterface;
import com.app.infrastructure.persistence.repository.spring.SpringRepository;
import com.app.infrastructure.security.auth.Auth0JWTHandler;
import com.app.infrastructure.security.auth.JWTAuthInterface;
import com.app.infrastructure.security.auth.RSAAlgorithm;
import com.app.infrastructure.security.hasher.HasherInterface;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;

@Configuration
public class ServiceContainer {
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private HasherInterface hasherInterface;

    @Bean
    public <T> RepositoryInterface<T> repository() {
        return new SpringRepository<T>(this.entityManager);
    }

    @Bean
    public UserService userService() {
        RepositoryInterface<User> userRepository = this.repository();
        userRepository.setEntity(User.class);
        return new UserService(userRepository, hasherInterface);
    }

    @Bean
    public JWTAuthInterface authInterface() {
        RSAAlgorithm rsaAlgorithm = new RSAAlgorithm();
        return new Auth0JWTHandler(rsaAlgorithm.getAlgorithm());
    }

    @Bean
    public CacheInterface cacheInterface() {
        JedisPool jedisPool = new JedisPool("localhost", 10001);

        return new JedisCache(jedisPool);
    }
}
