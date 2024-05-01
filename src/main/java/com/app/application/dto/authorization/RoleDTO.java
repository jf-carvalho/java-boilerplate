package com.app.application.dto.authorization;

import java.util.List;

public record RoleDTO(Long id, String name, List<PermissionDTO> permissions) {
}
