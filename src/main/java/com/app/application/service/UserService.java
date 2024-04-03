package com.app.application.service;

import com.app.domain.entity.User;
import com.app.infrastructure.persistence.repository.RepositoryInterface;

public class UserService {
    private final RepositoryInterface<User> userRepository;

    public UserService(RepositoryInterface<User> userRepository) {
        this.userRepository = userRepository;
    }
}
