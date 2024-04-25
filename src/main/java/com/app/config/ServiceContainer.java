package com.app.config;

import com.app.application.service.AuthService;
import com.app.application.service.UserService;
import com.app.application.util.AuthInterceptorHandler;
import com.app.infrastructure.cache.CacheInterface;
import com.app.infrastructure.cache.JedisCache;
import com.app.infrastructure.interceptor.AuthInterceptor;
import com.app.infrastructure.persistence.entity.User;
import com.app.infrastructure.persistence.repository.RepositoryInterface;
import com.app.infrastructure.persistence.repository.spring.SpringRepository;
import com.app.infrastructure.security.auth.Auth0JWTHandler;
import com.app.infrastructure.security.auth.JWTAuthInterface;
import com.app.infrastructure.security.auth.RSAAlgorithm;
import com.app.infrastructure.security.auth.exception.AuthException;
import com.app.infrastructure.security.hasher.HasherInterface;
import com.app.infrastructure.security.hasher.SpringBcryptHasher;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;

@Configuration
public class ServiceContainer {
    @Autowired
    private EntityManager entityManager;

    @Bean
    public <T> RepositoryInterface<T> repository() {
        return new SpringRepository<T>(this.entityManager);
    }

    @Bean
    public HasherInterface hasherInterface() {
        return new SpringBcryptHasher();
    }

    @Bean
    public UserService userService() {
        RepositoryInterface<User> userRepository = this.repository();
        userRepository.setEntity(User.class);
        return new UserService(userRepository, hasherInterface());
    }

    @Bean
    public JWTAuthInterface authInterface() {
        KeyFactory keyFactory = null;

        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new AuthException("Failed trying to instantiate key factory: " + e.getMessage());
        }

        URL keyFilesURL = Auth0JWTHandler.class.getClassLoader().getResource("keys");

        File keysDir = new File(keyFilesURL.getFile());
        String keysDecodeDir = URLDecoder.decode(keysDir.getAbsolutePath(), StandardCharsets.UTF_8);

        RSAAlgorithm rsaAlgorithm = new RSAAlgorithm(keyFactory, keysDecodeDir);
        return new Auth0JWTHandler(rsaAlgorithm.getAlgorithm());
    }

    @Bean
    public CacheInterface cacheInterface() {
        JedisPool jedisPool = new JedisPool("localhost", 10001);

        return new JedisCache(jedisPool);
    }

    @Bean
    public AuthService authService() {
        return new AuthService(
                authInterface(),
                userService(),
                hasherInterface(),
                cacheInterface()
        );
    }

    @Bean
    public AuthInterceptorHandler authInterceptorHandler() {
        return new AuthInterceptorHandler(this.authInterface(), this.cacheInterface());
    }

    @Bean
    public AuthInterceptor authInterceptor() {
        return new AuthInterceptor(authInterceptorHandler());
    }
}
