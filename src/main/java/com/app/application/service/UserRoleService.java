package com.app.application.service;

import com.app.application.dto.authorization.PermissionDTO;
import com.app.application.dto.authorization.RoleDTO;
import com.app.application.exception.ResourceNotFound;
import com.app.infrastructure.persistence.entity.Permission;
import com.app.infrastructure.persistence.entity.Role;
import com.app.infrastructure.persistence.entity.User;
import com.app.infrastructure.persistence.repository.RepositoryInterface;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserRoleService {
    private final RepositoryInterface<User> userRepository;
    private final RepositoryInterface<Role> roleRepository;

    public UserRoleService(
            RepositoryInterface<User> userRepository,
            RepositoryInterface<Role> roleRepository
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public List<RoleDTO> getUserRoles(Long userId) {
        User user = this.getUser(userId);

        Set<Role> roles = user.getRoles();

        List<RoleDTO> rolesDTOs = new ArrayList<>();

        roles.forEach(role -> {
            Set<Permission> rolePermissions = role.getPermissions();

            List<PermissionDTO> rolePermissionsDTOs = new ArrayList<>();

            rolePermissions.forEach(rolePermission -> {
                PermissionDTO permissionDTO = new PermissionDTO(rolePermission.getId(), rolePermission.getName());
                rolePermissionsDTOs.add(permissionDTO);
            });

            RoleDTO roleDTO = new RoleDTO(role.getId(), role.getName(), rolePermissionsDTOs);
            rolesDTOs.add(roleDTO);
        });

        return rolesDTOs;
    }

    public List<RoleDTO> syncUserRoles(Long userId, List<RoleDTO> roles) {
        User user = this.getUser(userId);

        user.getRoles().clear();

        Set<Role> rolesToUpdate = new HashSet<Role>();

        roles.forEach(roleDTO -> {
            Role role = this.roleRepository.getById(roleDTO.id());

            if (role != null) {
                rolesToUpdate.add(role);
            }
        });

        user.getRoles().addAll(rolesToUpdate);

        this.userRepository.update(userId, user);

        List<RoleDTO> rolesDTOs = new ArrayList<>();

        rolesToUpdate.forEach(updatedRole -> {
            Set<Permission> rolePermissions = updatedRole.getPermissions();

            List<PermissionDTO> rolePermissionsDTOs = new ArrayList<>();

            rolePermissions.forEach(rolePermission -> {
                PermissionDTO permissionDTO = new PermissionDTO(rolePermission.getId(), rolePermission.getName());
                rolePermissionsDTOs.add(permissionDTO);
            });

            RoleDTO roleDTO = new RoleDTO(updatedRole.getId(), updatedRole.getName(), rolePermissionsDTOs);
            rolesDTOs.add(roleDTO);
        });

        return rolesDTOs;
    }

    private User getUser(Long userId) {
        User user = this.userRepository.getById(userId);

        if (user == null) {
            throw new ResourceNotFound("Could not get roles because user was not found.");
        }

        return user;
    }
}
