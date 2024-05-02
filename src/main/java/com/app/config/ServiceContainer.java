package com.app.config;

import com.app.application.service.AuthService;
import com.app.application.service.UserRoleService;
import com.app.application.service.UserService;
import com.app.application.util.authentication.AuthInterceptorHandler;
import com.app.application.util.authorization.AuthorizationInterceptorHandler;
import com.app.infrastructure.cache.CacheInterface;
import com.app.infrastructure.cache.JedisCache;
import com.app.infrastructure.interceptor.AuthenticationInterceptor;
import com.app.infrastructure.interceptor.AuthorizationInterceptor;
import com.app.infrastructure.persistence.entity.Role;
import com.app.infrastructure.persistence.entity.User;
import com.app.infrastructure.persistence.repository.RepositoryInterface;
import com.app.infrastructure.persistence.repository.spring.SpringRepository;
import com.app.infrastructure.security.auth.*;
import com.app.infrastructure.security.auth.exception.AuthException;
import com.app.infrastructure.security.hasher.HasherInterface;
import com.app.infrastructure.security.hasher.SpringBcryptHasher;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
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
    @Scope("prototype")
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
                cacheInterface(),
                authHolder()
        );
    }

    @Bean
    public AuthHolderInterface authHolder(){
        return new SpringAuthHolder();
    }

    @Bean
    public AuthInterceptorHandler authInterceptorHandler() {
        return new AuthInterceptorHandler(
                this.authInterface(),
                this.cacheInterface(),
                this.userService(),
                authHolder()
        );
    }

    @Bean
    public AuthenticationInterceptor authInterceptor() {
        return new AuthenticationInterceptor(authInterceptorHandler());
    }

    @Bean
    public UserRoleService userRoleService() {
        RepositoryInterface<User> userRepository = repository();
        userRepository.setEntity(User.class);

        RepositoryInterface<Role> roleRepository = repository();
        roleRepository.setEntity(Role.class);

        return new UserRoleService(userRepository, roleRepository);
    }

    @Bean
    public AuthorizationInterceptorHandler authorizationInterceptorHandler() {
        return new AuthorizationInterceptorHandler(authHolder(), userRoleService());
    }

    @Bean
    public AuthorizationInterceptor authorizationInterceptor() {
        return new AuthorizationInterceptor(authorizationInterceptorHandler());
    }
}
