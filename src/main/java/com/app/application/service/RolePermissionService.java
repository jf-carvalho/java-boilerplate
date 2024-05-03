package com.app.application.service;

import com.app.application.dto.authorization.PermissionDTO;
import com.app.application.exception.ResourceNotFound;
import com.app.infrastructure.persistence.entity.Permission;
import com.app.infrastructure.persistence.entity.Role;
import com.app.infrastructure.persistence.repository.RepositoryInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RolePermissionService {
    private final RepositoryInterface<Role> roleRepository;

    public RolePermissionService(RepositoryInterface<Role> roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<PermissionDTO> getRolePermissions(Long roleId) {
        Role role = this.roleRepository.getById(roleId);

        if (role == null) {
            throw new ResourceNotFound("Could not get permissions because role was not found.");
        }

        Set<Permission> permissions = role.getPermissions();

        List<PermissionDTO> permissionsDTOs = new ArrayList<>();

        permissions.forEach(permission -> {
            PermissionDTO permissionDTO = new PermissionDTO(permission.getId(), permission.getName());
            permissionsDTOs.add(permissionDTO);
        });

        return permissionsDTOs;
    }
}
