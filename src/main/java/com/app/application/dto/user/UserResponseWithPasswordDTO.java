package com.app.application.dto.user;

public record UserResponseWithPasswordDTO(Long id, String name, String email, String password, String createdAt, String updatedAt, String deletedAt) {
}

