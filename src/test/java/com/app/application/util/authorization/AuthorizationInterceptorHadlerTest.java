package com.app.application.util.authorization;

import com.app.application.dto.authorization.PermissionDTO;
import com.app.application.dto.authorization.RoleDTO;
import com.app.application.service.UserRoleService;
import com.app.domain.entity.User;
import com.app.infrastructure.security.auth.AuthHolderInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class AuthorizationInterceptorHadlerTest {
    @Mock
    private AuthHolderInterface authHolder;

    @Mock
    private UserRoleService userRoleService;

    @InjectMocks
    private AuthorizationInterceptorHandler handler;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void adminUserShouldReturnTrue() {
        User user = new User(1L, "John Doe", "jdoe@domain.com");

        when(authHolder.getUser()).thenReturn(user);

        List<RoleDTO> roles = new ArrayList<>();
        roles.add(new RoleDTO(1L, RolesEnum.SUPER.toString(), new ArrayList<>()));

        when(userRoleService.getUserRoles(1L)).thenReturn(roles);

        assertTrue(handler.handle("foo"));
    }

    @Test
    public void nonAdminUserWithPermissionShouldReturnTrue() {
        User user = mock(User.class);
        when(authHolder.getUser()).thenReturn(user);

        List<PermissionDTO> permissions = new ArrayList<>();
        permissions.add(new PermissionDTO(1L, "foobarbaz"));

        List<RoleDTO> roles = new ArrayList<>();
        roles.add(new RoleDTO(1L, RolesEnum.COMMON.toString(), permissions));

        when(userRoleService.getUserRoles(any())).thenReturn(roles);

        assertTrue(handler.handle("foobarbaz"));
    }

    @Test
    public void nonAdminUserWithoutPermissionShouldReturnFalse() {
        User user = mock(User.class);
        when(authHolder.getUser()).thenReturn(user);

        List<PermissionDTO> permissions = new ArrayList<>();
        permissions.add(new PermissionDTO(1L, "foo"));

        List<RoleDTO> roles = new ArrayList<>();
        roles.add(new RoleDTO(1L, RolesEnum.COMMON.toString(), permissions));

        when(userRoleService.getUserRoles(any())).thenReturn(roles);

        assertFalse(handler.handle("lorem"));
    }
}
