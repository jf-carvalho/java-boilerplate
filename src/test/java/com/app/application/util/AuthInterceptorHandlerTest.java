package com.app.application.util;

import com.app.application.dto.auth.JwtClaimDTO;
import com.app.application.dto.user.UserResponseDTO;
import com.app.application.exception.UnauthenticatedException;
import com.app.application.service.UserService;
import com.app.domain.entity.User;
import com.app.infrastructure.cache.CacheInterface;
import com.app.infrastructure.security.auth.AuthHolderInterface;
import com.app.infrastructure.security.auth.JWTAuthInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class AuthInterceptorHandlerTest {
    @Mock
    private JWTAuthInterface auth;

    @Mock
    private CacheInterface cache;

    @Mock
    private UserService userService;

    @Mock
    private AuthHolderInterface authHolder;

    @InjectMocks
    private AuthInterceptorHandler authInterceptorHandler;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldHandleAuthenticatedUser() {
        String authHeader = "Bearer eycv7tn0eq8qeymrghdsjgg.8b74nv9ae8aybnegy34780tq2t1";

        when(this.auth.validateToken(authHeader.replace("Bearer ", ""))).thenReturn(true);
        when(this.cache.getList("auth_tokens_blacklist")).thenReturn(new HashSet<>());

        List<JwtClaimDTO> claims = new ArrayList<JwtClaimDTO>();
        claims.add(new JwtClaimDTO("expiresAt", "3000-01-01 00:00:00.000000"));
        claims.add(new JwtClaimDTO("userId", "1"));

        when(auth.getClaims()).thenReturn(claims);

        when(userService.get(1L)).thenReturn(new UserResponseDTO(1L, "John Doe", "jdoe@domain.com", "", "", ""));

        authInterceptorHandler.handle(authHeader);

        ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
        verify(authHolder).setAuth(argument.capture(), any(String.class));

        assertEquals(argument.getValue().getName(), "John Doe");
        assertEquals(argument.getValue().getEmail(), "jdoe@domain.com");
    }

    @ParameterizedTest
    @MethodSource("invalidAuthHeaders")
    public void shouldThrowException_withInvalidAuthHeader(String authHeader) {
        UnauthenticatedException exception = assertThrows(
                UnauthenticatedException.class,
                () -> this.authInterceptorHandler.handle(authHeader)
        );

        assertEquals("Auth header is empty.", exception.getMessage());
    }

    static Stream<Arguments> invalidAuthHeaders() {
        return Stream.of(
                null,
                Arguments.of(""),
                Arguments.of("      ")
        );
    }

    @Test
    public void shouldThgrowException_withInvalidatedToken() {
        String authHeader = "Bearer eycv7tn0eq8qeymrghdsjgg.8b74nv9ae8aybnegy34780tq2t1";

        when(this.auth.validateToken(authHeader.replace("Bearer ", ""))).thenReturn(false);

        UnauthenticatedException exception = assertThrows(
                UnauthenticatedException.class,
                () -> this.authInterceptorHandler.handle(authHeader)
        );

        assertEquals("Token has invalid content.", exception.getMessage());
    }

    @Test
    public void shouldThrowException_withBlacklistedToken() {
        String authHeader = "Bearer eycv7tn0eq8qeymrghdsjgg.8b74nv9ae8aybnegy34780tq2t1";
        String token = authHeader.replace("Bearer ", "");
        when(this.auth.validateToken(token)).thenReturn(true);

        Set<String> blackListedTokens = new HashSet<>();
        blackListedTokens.add(token);

        when(this.cache.getList("auth_tokens_blacklist")).thenReturn(blackListedTokens);

        UnauthenticatedException exception = assertThrows(
                UnauthenticatedException.class,
                () -> this.authInterceptorHandler.handle(authHeader)
        );

        assertEquals("Provided token is blacklisted.", exception.getMessage());
    }

    @Test
    public void shouldThrowException_withExpirationClaimAbsent() {
        String authHeader = "Bearer eycv7tn0eq8qeymrghdsjgg.8b74nv9ae8aybnegy34780tq2t1";
        when(this.auth.validateToken(authHeader.replace("Bearer ", ""))).thenReturn(true);

        when(auth.getClaims()).thenReturn(new ArrayList<JwtClaimDTO>());

        UnauthenticatedException exception = assertThrows(
                UnauthenticatedException.class,
                () -> this.authInterceptorHandler.handle(authHeader)
        );

        assertEquals("Token's expiration not set.", exception.getMessage());
    }

    @Test
    public void shouldThrowException_withInvalidExpirationClaim() {
        String authHeader = "Bearer eycv7tn0eq8qeymrghdsjgg.8b74nv9ae8aybnegy34780tq2t1";
        when(this.auth.validateToken(authHeader.replace("Bearer ", ""))).thenReturn(true);

        List<JwtClaimDTO> claims = new ArrayList<JwtClaimDTO>();
        claims.add(new JwtClaimDTO("expiresAt", "foobarbaz"));

        when(auth.getClaims()).thenReturn(claims);

        UnauthenticatedException exception = assertThrows(
                UnauthenticatedException.class,
                () -> this.authInterceptorHandler.handle(authHeader)
        );

        assertEquals("Date parsing failed.", exception.getMessage());
    }

    @Test
    public void shouldThrowException_withExpiredClaim() {
        String authHeader = "Bearer eycv7tn0eq8qeymrghdsjgg.8b74nv9ae8aybnegy34780tq2t1";
        when(this.auth.validateToken(authHeader.replace("Bearer ", ""))).thenReturn(true);

        List<JwtClaimDTO> claims = new ArrayList<JwtClaimDTO>();
        claims.add(new JwtClaimDTO("expiresAt", "1500-01-01 00:00:00.000000"));

        when(auth.getClaims()).thenReturn(claims);

        UnauthenticatedException exception = assertThrows(
                UnauthenticatedException.class,
                () -> this.authInterceptorHandler.handle(authHeader)
        );

        assertEquals("Provided token is expired.", exception.getMessage());
    }

    @Test
    public void shouldThrowException_withUserClaimAbsent() {
        String authHeader = "Bearer eycv7tn0eq8qeymrghdsjgg.8b74nv9ae8aybnegy34780tq2t1";
        when(this.auth.validateToken(authHeader.replace("Bearer ", ""))).thenReturn(true);

        List<JwtClaimDTO> claims = new ArrayList<JwtClaimDTO>();
        claims.add(new JwtClaimDTO("expiresAt", "3000-01-01 00:00:00.000000"));

        when(auth.getClaims()).thenReturn(claims);

        UnauthenticatedException exception = assertThrows(
                UnauthenticatedException.class,
                () -> this.authInterceptorHandler.handle(authHeader)
        );

        assertEquals("Token does not carry an user id.", exception.getMessage());
    }
}
