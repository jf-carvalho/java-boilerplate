package com.app.infrastructure.security.auth;

import com.app.application.dto.auth.JwtClaimDTO;
import com.app.infrastructure.security.auth.exception.AuthException;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Auth0JWTHandlerTest {
    private Auth0JWTHandler jwtHandler;
    private String validToken;

    @BeforeEach
    public void setup() {
        Algorithm algorithm = Algorithm.HMAC256("secret");
        jwtHandler = new Auth0JWTHandler(algorithm);
        validToken = jwtHandler.createToken(new ArrayList<>());
    }

    @Test
    public void shouldCreateToken() {
        String token = jwtHandler.createToken(new ArrayList<>());
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    public void shouldCreateTokenWithClaims() {
        List<JwtClaimDTO> claims = new ArrayList<>();
        claims.add(new JwtClaimDTO("foo", "bar"));

        String token = jwtHandler.createToken(claims);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    public void shouldNotCreateToken() {
        Auth0JWTHandler invalidJwtHandler = new Auth0JWTHandler(null);
        assertThrows(AuthException.class, () -> invalidJwtHandler.createToken(new ArrayList<>()), "JWT token creation failed.");
    }

    @Test
    public void shouldValidateToken() {
        boolean isValid = jwtHandler.validateToken(validToken);
        assertTrue(isValid);
    }

    @Test
    public void shouldNotValidateToken() {
        assertThrows(AuthException.class, () -> jwtHandler.validateToken("invalid_token"), "Token validation failed");
    }

    @Test
    public void shouldGetClaims() {
        jwtHandler.validateToken(validToken);
        List<JwtClaimDTO> claims = this.jwtHandler.getClaims();

        assertEquals(claims.getFirst().key(), "iss");
        assertEquals(claims.getFirst().value(), "auth0");
    }

    @Test
    public void shouldNotGetClaims() {
        assertThrows(AuthException.class, this.jwtHandler::getClaims, "JWT must be decoded in order to get it's claims.");
    }
}
