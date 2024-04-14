package com.app.application.dto.user;

public record UserResponseDTO(Long id, String name, String email, String createdAt, String updatedAt, String deletedAt) {
}
