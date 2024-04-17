package com.app.infrastructure.security.auth;

import com.app.infrastructure.security.auth.exception.AuthException;

public interface JWTAuthInterface {
    String createToken() throws AuthException;
    boolean validateToken(String token);
}
