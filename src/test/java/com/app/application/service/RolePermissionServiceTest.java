package com.app.application.service;

import com.app.application.dto.authorization.PermissionDTO;
import com.app.application.exception.ResourceNotFound;
import com.app.infrastructure.persistence.entity.Permission;
import com.app.infrastructure.persistence.entity.Role;
import com.app.infrastructure.persistence.repository.RepositoryInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

public class RolePermissionServiceTest {
    @Mock
    private RepositoryInterface<Role> roleRepository;

    @InjectMocks
    private RolePermissionService rolePermissionService;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        this.roleRepository.setEntity(Role.class);
    }


    @Test
    public void shouldGetUserRoles() {
        Role foundRole = mock(Role.class);

        when(roleRepository.getById(1L)).thenReturn(foundRole);

        Set<Permission> permissions = new HashSet<>();
        permissions.add(new Permission(1L, "foo"));

        when(foundRole.getPermissions()).thenReturn(permissions);

        List<PermissionDTO> rolePermissions = this.rolePermissionService.getRolePermissions(1L);

        verify(foundRole).getPermissions();

        assertEquals(rolePermissions.getFirst().getClass(), PermissionDTO.class);
    }

    @Test
    public void shouldNotGetUsers_whenUserNotFound() {
        when(roleRepository.getById(1L)).thenReturn(null);

        ResourceNotFound exception = assertThrows(ResourceNotFound.class, () -> this.rolePermissionService.getRolePermissions(1L));

        assertEquals("Could not get permissions because role was not found.", exception.getMessage());
    }
}
