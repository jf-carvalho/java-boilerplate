package com.kleanarchapi.application.service;

import com.kleanarchapi.domain.entity.User;
import com.kleanarchapi.infrastructure.persistence.repository.RepositoryInterface;

public class UserService {
    private final RepositoryInterface<User> userRepository;

    public UserService(RepositoryInterface<User> userRepository) {
        this.userRepository = userRepository;
    }
}
