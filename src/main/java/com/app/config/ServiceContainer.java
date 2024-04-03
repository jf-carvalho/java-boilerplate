package com.app.config;

import com.app.application.service.UserService;
import com.app.domain.entity.User;
import com.app.infrastructure.persistence.repository.RepositoryInterface;
import com.app.infrastructure.persistence.repository.spring.SpringRepository;
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
