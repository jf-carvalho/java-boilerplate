package com.app.application.service;

import com.app.application.dto.auth.LoginRequestDTO;
import com.app.application.dto.auth.LoginResponseDTO;
import com.app.application.dto.user.UserResponseWithPasswordDTO;
import com.app.application.exception.ResourceNotFound;
import com.app.application.exception.UnauthenticatedException;
import com.app.infrastructure.cache.CacheInterface;
import com.app.infrastructure.security.auth.JWTAuthInterface;
import com.app.infrastructure.security.hasher.HasherInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {
    @Mock
    private JWTAuthInterface auth;

    @Mock
    private UserService userService;

    @Mock
    private HasherInterface hasher;

    @Mock
    private CacheInterface cache;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void validLoginShouldGenerateToken() {
        UserResponseWithPasswordDTO foundUser = new UserResponseWithPasswordDTO(
                1L,
                "John Doe",
                "jdoe@domain.com",
                "some_hashed_password",
                "2024-04-04 00:00:00",
                "2024-04-04 00:00:00",
                null
        );

        when(userService.getByEmail("jdoe@domain.com")).thenReturn(foundUser);
        when(hasher.checkHash("some_hashed_password", "Password1")).thenReturn(true);
        when(cache.get(foundUser.id().toString() + "_current_token")).thenReturn(null);
        when(auth.createToken()).thenReturn("valid_json_web_token");

        LoginRequestDTO loginDTO = new LoginRequestDTO("jdoe@domain.com", "Password1");
        authService.attemptLogin(loginDTO);

        verify(cache).set(foundUser.id().toString() + "_current_token", "valid_json_web_token");
    }

    @Test
    public void loginUserNotFoundShouldThrowException() {
        when(userService.getByEmail("jdoe@domain.com")).thenThrow(ResourceNotFound.class);

        LoginRequestDTO loginDTO = new LoginRequestDTO("jdoe@domain.com", "Password1");

        assertThrows(ResourceNotFound.class, () -> authService.attemptLogin(loginDTO));
    }

    @Test
    public void wrongPasswordShouldThrowException() {
        UserResponseWithPasswordDTO foundUser = new UserResponseWithPasswordDTO(
                1L,
                "John Doe",
                "jdoe@domain.com",
                "some_hashed_password",
                "2024-04-04 00:00:00",
                "2024-04-04 00:00:00",
                null
        );

        when(userService.getByEmail("jdoe@domain.com")).thenReturn(foundUser);
        when(hasher.checkHash("some_hashed_password", "Password1")).thenReturn(false);

        LoginRequestDTO loginDTO = new LoginRequestDTO("jdoe@domain.com", "Password1");
        assertThrows(UnauthenticatedException.class, () -> authService.attemptLogin(loginDTO), "Wrong credentials.");
    }

    @Test
    public void shouldBlacklistCurrentUserToken_whenCreatingNew() {
        UserResponseWithPasswordDTO foundUser = new UserResponseWithPasswordDTO(
                1L,
                "John Doe",
                "jdoe@domain.com",
                "some_hashed_password",
                "2024-04-04 00:00:00",
                "2024-04-04 00:00:00",
                null
        );

        when(userService.getByEmail("jdoe@domain.com")).thenReturn(foundUser);
        when(hasher.checkHash("some_hashed_password", "Password1")).thenReturn(true);
        when(cache.get(foundUser.id().toString() + "_current_token")).thenReturn("current_valid_jwt");
        when(cache.add("auth_tokens_blacklist", "current_valid_jwt")).thenReturn(true);
        when(auth.createToken()).thenReturn("valid_json_web_token");

        LoginRequestDTO loginDTO = new LoginRequestDTO("jdoe@domain.com", "Password1");
        LoginResponseDTO loginResponseDTO = authService.attemptLogin(loginDTO);
        assertEquals(loginResponseDTO.accessToken(), "valid_json_web_token");

        verify(cache).set(foundUser.id().toString() + "_current_token", "valid_json_web_token");
    }

    @Test
    public void shouldLogout_withValidToken() {

    }

    @Test
    public void shouldNotLogout_withInvalidToken() {

    }
}
