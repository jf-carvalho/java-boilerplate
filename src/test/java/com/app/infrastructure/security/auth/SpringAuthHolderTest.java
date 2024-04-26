package com.app.infrastructure.security.auth;

import com.app.domain.entity.User;
import com.app.infrastructure.security.auth.exception.AuthException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SpringAuthHolderTest {
    private SpringAuthHolder authHolder;

    @BeforeEach
    public void init() {
        this.authHolder = new SpringAuthHolder();
    }

    @Test
    public void shouldSetAndGetAuthenticatedUser() {
        User user = new User(1L, "John Doe", "jdoe@doamin.com");
        this.authHolder.setAuth(user, "user_token");

        User authenticatedUser = this.authHolder.getUser();

        assertEquals(user, authenticatedUser);

        String authenticatedToken = this.authHolder.getToken();
        assertEquals(authenticatedToken, "user_token");
    }

    @Test
    public void shouldNotGetUser_whenNoUserAuthenticated() {
        assertThrows(AuthException.class, this.authHolder::getUser, "There is no authenticated user");
        assertThrows(AuthException.class, this.authHolder::getToken, "There is no authenticated user");
    }


}
