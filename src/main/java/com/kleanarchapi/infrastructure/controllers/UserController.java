package com.kleanarchapi.infrastructure.controllers;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.EntityManager;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private EntityManager entityManager;

    @GetMapping
    @Transactional
    public void get() {

    }
}
