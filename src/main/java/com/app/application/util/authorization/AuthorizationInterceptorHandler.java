package com.app.application.util.authorization;

import com.app.application.dto.authorization.PermissionDTO;
import com.app.application.dto.authorization.RoleDTO;
import com.app.application.service.UserRoleService;
import com.app.domain.entity.User;
import com.app.infrastructure.security.auth.AuthHolderInterface;

import java.util.List;

public class AuthorizationInterceptorHandler {
    private final AuthHolderInterface authHolder;
    private final UserRoleService userRoleService;

    public AuthorizationInterceptorHandler(AuthHolderInterface authHolder, UserRoleService userRoleService) {
        this.authHolder = authHolder;
        this.userRoleService = userRoleService;
    }

    public boolean handle(String action) {
        User user = this.authHolder.getUser();

        List<RoleDTO> roles = this.userRoleService.getUserRoles(user.getId());

        boolean isSuper = roles.stream()
                .anyMatch(role -> RolesEnum.SUPER.toString().equalsIgnoreCase(role.name()));

        if (isSuper) {
            return true;
        }

        List<String> uniquePermissionNames = roles.stream()
                .flatMap(role -> role.permissions().stream())
                .map(PermissionDTO::name)
                .distinct()
                .toList();

        return uniquePermissionNames.contains(action);
    }
}
