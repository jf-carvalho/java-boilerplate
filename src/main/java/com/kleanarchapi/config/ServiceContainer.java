package com.kleanarchapi.config;

import com.kleanarchapi.application.service.UserService;
import com.kleanarchapi.domain.entity.User;
import com.kleanarchapi.infrastructure.persistence.repository.RepositoryInterface;
import com.kleanarchapi.infrastructure.persistence.repository.spring.SpringRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceContainer {
    @Bean
    public <T> RepositoryInterface<T> repository() {
        return new SpringRepository<T>();
    }

    @Bean
    public UserService userService() {
        RepositoryInterface<User> userRepository = this.repository();
        userRepository.setEntity(User.class);
        return new UserService(userRepository);
    }
}
