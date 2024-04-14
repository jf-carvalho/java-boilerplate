package com.app.config;

import com.app.infrastructure.security.hasher.HasherInterface;
import com.app.infrastructure.security.hasher.SpringBcryptHasher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UtilConfig {

    @Bean
    public HasherInterface hasherInterface() {
        return new SpringBcryptHasher();
    }
}
