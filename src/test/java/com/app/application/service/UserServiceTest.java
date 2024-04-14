package com.app.application.service;

import com.app.application.dto.user.UpdatePasswordDTO;
import com.app.application.dto.user.UserRequestDTO;
import com.app.application.dto.user.UserResponseDTO;
import com.app.application.exception.IncorrectPasswordException;
import com.app.application.exception.ResourceNotFound;
import com.app.domain.exception.UserException;
import com.app.infrastructure.persistence.criteria.Criteria;
import com.app.infrastructure.persistence.entity.User;
import com.app.infrastructure.persistence.exceptions.EntityNotFoundException;
import com.app.infrastructure.persistence.repository.RepositoryInterface;
import com.app.infrastructure.security.hasher.HasherInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {
    @Mock
    private RepositoryInterface<User> userRepository;

    @Mock
    private HasherInterface hasherInterface;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        userRepository.setEntity(User.class);
    }

    @Test
    public void shouldGetUser_whenExists() {
        User user = new User(1L, "John Doe", "jdoe@domain.com");

        when(userRepository.getById(1L)).thenReturn(user);

        UserResponseDTO userResponseDTO = userService.get(1L);

        assertEquals(userResponseDTO.id(), user.getId());
        assertEquals(userResponseDTO.name(), user.getName());
        assertEquals(userResponseDTO.email(), user.getEmail());
    }

    @Test
    public void shouldNotGetUser_whenItDoesNotExists() {
        when(userRepository.getById(1L)).thenReturn(null);

        assertThrows(ResourceNotFound.class, () -> userService.get(1L), "User with id 1L not found.");
    }

    @Test
    public void shouldGetAllUsers() {
        User user1 = new User(1L, "John Doe", "jdoe@domain.com");
        User user2 = new User(2L, "Jane Doe", "janedoe@domain.com");
        User user3 = new User(3L, "Jack Doe", "jackdoe@domain.com");

        List<User> users = new ArrayList<User>();
        users.add(user1);
        users.add(user2);
        users.add(user3);

        when(userRepository.getAll()).thenReturn(users);

        List<UserResponseDTO> usersDTOs = userService.getAll();

        assertEquals(users.get(0).getId(), usersDTOs.get(0).id());
        assertEquals(users.get(0).getName(), usersDTOs.get(0).name());
        assertEquals(users.get(0).getEmail(), usersDTOs.get(0).email());

        assertEquals(users.get(1).getId(), usersDTOs.get(1).id());
        assertEquals(users.get(1).getName(), usersDTOs.get(1).name());
        assertEquals(users.get(1).getEmail(), usersDTOs.get(1).email());

        assertEquals(users.get(2).getId(), usersDTOs.get(2).id());
        assertEquals(users.get(2).getName(), usersDTOs.get(2).name());
        assertEquals(users.get(2).getEmail(), usersDTOs.get(2).email());
    }

    @Test
    public void shouldCreateUser() {
        when(hasherInterface.getSalt()).thenReturn("random_salt");
        when(hasherInterface.getHash("Password1", "random_salt")).thenReturn("tH1$k1nd4L00k$l1k34h4$h");

        User savedUser = new User("John Doe", "jdoe@domain.com", "tH1$k1nd4L00k$l1k34h4$h");
        when(userRepository.create(any(User.class))).thenReturn(savedUser);

        UserRequestDTO userRequestDTO = new UserRequestDTO("John Doe", "jdoe@domain.com", "Password1");

        UserResponseDTO result = userService.create(userRequestDTO);

        verify(hasherInterface).getSalt();
        verify(hasherInterface).getHash("Password1", "random_salt");
        verify(userRepository).create(any(User.class));

        assertEquals(savedUser.getId(), result.id());
        assertEquals(savedUser.getName(), result.name());
        assertEquals(savedUser.getEmail(), result.email());
    }

    @Test
    public void shouldNotCreateUser() {
        UserRequestDTO userRequestDTO = new UserRequestDTO("", "jdoe@domain.com", "Password1");

        assertThrows(UserException.class, () -> {userService.create(userRequestDTO);});
    }

    @Test
    public void shouldUpdateUser() {
        User savedUser = new User(1L, "John Doe", "jdoe@domain.com");

        when(userRepository.update(any(Long.class), any(User.class))).thenReturn(savedUser);

        UserRequestDTO userRequestDTO = new UserRequestDTO("John Doe", "jdoe@domain.com", "Password1");

        UserResponseDTO result = userService.update(1L, userRequestDTO);

        verify(userRepository).update(any(Long.class), any(User.class));

        assertEquals(savedUser.getId(), result.id());
        assertEquals(savedUser.getName(), result.name());
        assertEquals(savedUser.getEmail(), result.email());
    }

    @Test
    public void shouldNotUpdateUser() {
        UserRequestDTO userRequestDTO = new UserRequestDTO("John Doe", "jdoe@domain.com", "Password1");

        assertThrows(UserException.class, () -> userService.update(null, userRequestDTO));
    }

    @Test
    public void shouldUpdatePassword() {
        Criteria criteria = new Criteria();
        criteria.equals("email", "jdoe@domain.com");

        User foundUser = new User(1L, "John Doe", "jdoe@domain.com", "1234657");

        List<User> matchingUsers = new ArrayList<User>();
        matchingUsers.add(foundUser);

        when(userRepository.getByFilter(any(Criteria.class))).thenReturn(matchingUsers);

        when(hasherInterface.checkHash(any(String.class), any(String.class))).thenReturn(true);
        when(hasherInterface.getSalt()).thenReturn("random_salt");
        when(hasherInterface.getHash("Password1", "random_salt")).thenReturn("tH1$k1nd4L00k$l1k34h4$h");

        User savedUser = new User(1L, "John Doe", "jdoe@domain.com");

        when(userRepository.update(any(Long.class), any(User.class))).thenReturn(savedUser);

        UpdatePasswordDTO updatePasswordDTO = new UpdatePasswordDTO(
                "jdoe@domain.com",
                "123456",
                "Password1"
        );

        userService.updatePassword(updatePasswordDTO);
    }

    @Test
    public void shouldNotUpdatePassword_withInvalidUserEmail() {
        when(userRepository.getByFilter(any(Criteria.class))).thenReturn(new ArrayList<>());

        UpdatePasswordDTO updatePasswordDTO = new UpdatePasswordDTO(
                "jdoe@domain.com",
                "123456",
                "Password1"
        );

        assertThrows(
                ResourceNotFound.class,
                () -> userService.updatePassword(updatePasswordDTO),
                "User with provided email not found."
        );
    }

    @Test
    public void shouldNotUpdatePassword_withIncorrectPassword() {
        User foundUser = new User(1L, "John Doe", "jdoe@domain.com", "1234657");

        List<User> matchingUsers = new ArrayList<User>();
        matchingUsers.add(foundUser);

        when(userRepository.getByFilter(any(Criteria.class))).thenReturn(matchingUsers);

        when(hasherInterface.checkHash(any(String.class), any(String.class))).thenReturn(false);

        UpdatePasswordDTO updatePasswordDTO = new UpdatePasswordDTO(
                "jdoe@domain.com",
                "123456",
                "Password1"
        );

        assertThrows(
                IncorrectPasswordException.class,
                () -> userService.updatePassword(updatePasswordDTO)
        );
    }

    @Test
    public void shouldSoftDeleteUser() {
        when(userRepository.update(any(Long.class), any(User.class))).thenReturn(new User());

        assertTrue(userService.softDelete(1L));
    }

    @Test
    public void shouldNotSoftDeleteUser() {
        when(userRepository.update(any(Long.class), any(User.class))).thenThrow(EntityNotFoundException.class);

        assertFalse(userService.softDelete(1L));
    }

    @Test
    public void shouldRestoreUser() {
        when(userRepository.update(any(Long.class), any(User.class))).thenReturn(new User());

        assertTrue(userService.restoreUser(1L));
    }

    @Test
    public void shouldNotRestoreUser() {
        when(userRepository.update(any(Long.class), any(User.class))).thenThrow(EntityNotFoundException.class);

        assertFalse(userService.restoreUser(1L));
    }

    @Test
    public void shouldDeleteUser() {
        when(userRepository.delete(any(Long.class))).thenReturn(true);

        assertTrue(userService.deleteUser(1L));
    }

    @Test
    public void shouldNotDeleteUser() {
        when(userRepository.delete(any(Long.class))).thenThrow(EntityNotFoundException.class);

        assertFalse(userService.deleteUser(1L));
    }
}