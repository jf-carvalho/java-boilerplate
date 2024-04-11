package com.app.config;

import com.app.application.service.UserService;
import com.app.domain.entity.User;
import com.app.infrastructure.persistence.repository.RepositoryInterface;
import com.app.infrastructure.persistence.repository.spring.SpringRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceContainer {
    @Autowired
    private EntityManager entityManager;

    @Bean
    public <T> RepositoryInterface<T> repository() {
        return new SpringRepository<T>(this.entityManager);
    }

    @Bean
    public UserService userService() {
        RepositoryInterface<User> userRepository = this.repository();
        userRepository.setEntity(User.class);
        return new UserService(userRepository);
    }
}
