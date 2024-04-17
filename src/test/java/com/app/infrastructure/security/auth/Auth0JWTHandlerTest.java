package com.app.infrastructure.security.auth;

import com.app.infrastructure.security.auth.exception.AuthException;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Auth0JWTHandlerTest {
    private Auth0JWTHandler jwtHandler;
    private String validToken;

    @BeforeEach
    public void setup() {
        Algorithm algorithm = Algorithm.HMAC256("secret");
        jwtHandler = new Auth0JWTHandler(algorithm);
        validToken = jwtHandler.createToken();
    }

    @Test
    public void testCreateTokenSuccess() {
        String token = jwtHandler.createToken();
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    public void testCreateTokenFailure() {
        Auth0JWTHandler invalidJwtHandler = new Auth0JWTHandler(null);
        assertThrows(AuthException.class, invalidJwtHandler::createToken, "JWT token creation failed.");
    }

    @Test
    public void testValidateTokenSuccess() {
        boolean isValid = jwtHandler.validateToken(validToken);
        assertTrue(isValid);
    }

    @Test
    public void testValidateTokenFailure() {
        assertThrows(AuthException.class, () -> jwtHandler.validateToken("invalid_token"), "Token validation failed");
    }

    @Test
    public void testValidateTokenException() {
        AuthException exception = assertThrows(AuthException.class, () -> jwtHandler.validateToken(null));
        assertEquals("Token validation failed", exception.getMessage());
    }
}
