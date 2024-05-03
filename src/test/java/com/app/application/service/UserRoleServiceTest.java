package com.app.application.service;

import com.app.application.dto.authorization.PermissionDTO;
import com.app.application.dto.authorization.RoleDTO;
import com.app.application.exception.ResourceNotFound;
import com.app.infrastructure.persistence.entity.Permission;
import com.app.infrastructure.persistence.entity.Role;
import com.app.infrastructure.persistence.entity.User;
import com.app.infrastructure.persistence.repository.RepositoryInterface;
import com.app.infrastructure.persistence.repository.spring.SpringRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserRoleServiceTest {
    private RepositoryInterface<User> userRepository;

    private RepositoryInterface<Role> roleRepository;

    private UserRoleService userRoleService;

    @BeforeEach
    public void init() {
        RepositoryInterface<User> userRepository = mock(SpringRepository.class);
        userRepository.setEntity(User.class);
        this.userRepository = userRepository;

        RepositoryInterface<Role> roleRepository = mock(SpringRepository.class);
        roleRepository.setEntity(Role.class);
        this.roleRepository = roleRepository;

        this.userRoleService = new UserRoleService(userRepository, roleRepository);
    }


    @Test
    public void shouldGetUserRoles() {
        User foundUser = mock(User.class);

        when(userRepository.getById(1L)).thenReturn(foundUser);

        Set<Role> roles = new HashSet<>();
        roles.add(new Role(1L, "foo"));

        when(foundUser.getRoles()).thenReturn(roles);

        List<RoleDTO> userRoles = this.userRoleService.getUserRoles(1L);

        verify(foundUser).getRoles();

        assertEquals(userRoles.getFirst().getClass(), RoleDTO.class);
    }


    @Test
    public void shouldGetUserRolesWithPermissions() {
        User foundUser = mock(User.class);

        when(userRepository.getById(1L)).thenReturn(foundUser);

        Role role = mock(Role.class);
        Set<Role> roles = new HashSet<>();
        roles.add(role);

        Set<Permission> permissions = new HashSet<>();
        Permission foundPermission = new Permission(1L, "foo");
        permissions.add(foundPermission);

        when(role.getPermissions()).thenReturn(permissions);
        when(foundUser.getRoles()).thenReturn(roles);

        List<RoleDTO> userRoles = this.userRoleService.getUserRoles(1L);

        verify(foundUser).getRoles();

        assertEquals(userRoles.getFirst().getClass(), RoleDTO.class);
        assertEquals(userRoles.getFirst().permissions().getFirst().getClass(), PermissionDTO.class);
        assertEquals(userRoles.getFirst().permissions().getFirst().id(), foundPermission.getId());
        assertEquals(userRoles.getFirst().permissions().getFirst().name(), foundPermission.getName());
    }

    @Test
    public void shouldNotGetUsers_whenUserNotFound() {
        when(userRepository.getById(1L)).thenReturn(null);

        ResourceNotFound exception = assertThrows(ResourceNotFound.class, () -> this.userRoleService.getUserRoles(1L));

        assertEquals("Could not get roles because user was not found.", exception.getMessage());
    }

    @Test
    public void shouldSyncUserRoles() {
        User foundUser = mock(User.class);

        when(userRepository.getById(1L)).thenReturn(foundUser);

        Role foundRole1 = mock(Role.class);
        Role foundRole2 = mock(Role.class);

        List<RoleDTO> rolesDTOs = new ArrayList<>();

        List<PermissionDTO> role1Permissions = new ArrayList<PermissionDTO>();
        role1Permissions.add(new PermissionDTO(1L, "foo-permission"));
        RoleDTO role1 = new RoleDTO(1L, "foo", role1Permissions);
        rolesDTOs.add(role1);

        Set<Permission> role1FoundPermissions = new HashSet<>();
        role1FoundPermissions.add(new Permission(1L, "foo-permission"));
        when(foundRole1.getPermissions()).thenReturn(role1FoundPermissions);

        List<PermissionDTO> role2Permissions = new ArrayList<PermissionDTO>();
        role2Permissions.add(new PermissionDTO(2L, "bar-permission"));
        RoleDTO role2 = new RoleDTO(2L, "bar", role2Permissions);
        rolesDTOs.add(role2);

        when(roleRepository.getById(1L)).thenReturn(foundRole1);
        when(roleRepository.getById(2L)).thenReturn(foundRole2);
        when(roleRepository.getById(3L)).thenReturn(null);

        Set<Role> foundRoles = mock(HashSet.class);
        when(foundUser.getRoles()).thenReturn(foundRoles);

        List<RoleDTO> roles = this.userRoleService.syncUserRoles(1L, rolesDTOs);

        verify(this.userRepository).update(1L, foundUser);
        verify(foundUser, times(2)).getRoles();
        verify(foundRoles).clear();
        verify(foundRoles).addAll(any());

        assertEquals( 2, roles.size());

        RoleDTO firstRole = roles.getFirst();

        assertEquals(firstRole.permissions().get(0).getClass(), PermissionDTO.class);
        assertEquals(firstRole.permissions().get(0).id(), 1L);
        assertEquals(firstRole.permissions().get(0).name(), "foo-permission");
    }

    @Test
    public void shouldNotSyncRoles_whenUserNotFound() {
        when(userRepository.getById(1L)).thenReturn(null);

        ResourceNotFound exception = assertThrows(ResourceNotFound.class, () -> this.userRoleService.syncUserRoles(1L, any(List.class)));

        assertEquals("Could not get roles because user was not found.", exception.getMessage());
    }
}
