package com.app.application.dto.user;

public record UpdatePasswordDTO(String email, String oldPassword, String newPassword) {
}
