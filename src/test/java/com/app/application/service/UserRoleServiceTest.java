package com.app.application.service;

import com.app.application.dto.authorization.RoleDTO;
import com.app.application.exception.ResourceNotFound;
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
    public void shouldNotGetUsers_whenUserNotFound() {
        when(userRepository.getById(1L)).thenReturn(null);

        ResourceNotFound exception = assertThrows(ResourceNotFound.class, () -> this.userRoleService.getUserRoles(1L));

        assertEquals("Could not get roles because user was not found.", exception.getMessage());
    }

    @Test
    public void shouldSyncUserRoles() {
        User foundUser = mock(User.class);

        when(userRepository.getById(1L)).thenReturn(foundUser);

        Role foundRole1 = new Role(1L, "foo");
        Role foundRole2 = new Role(2L, "bar");

        List<RoleDTO> rolesDTOs = new ArrayList<>();
        rolesDTOs.add(new RoleDTO(1L, "foo"));
        rolesDTOs.add(new RoleDTO(2L, "bar"));
        rolesDTOs.add(new RoleDTO(3L, "baz"));

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
    }

    @Test
    public void shouldNotSyncRoles_whenUserNotFound() {
        when(userRepository.getById(1L)).thenReturn(null);

        ResourceNotFound exception = assertThrows(ResourceNotFound.class, () -> this.userRoleService.syncUserRoles(1L, any(List.class)));

        assertEquals("Could not get roles because user was not found.", exception.getMessage());
    }
}
