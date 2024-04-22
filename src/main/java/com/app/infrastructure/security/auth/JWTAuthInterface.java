package com.app.infrastructure.security.auth;

import com.app.application.dto.auth.JwtClaimDTO;
import com.app.infrastructure.security.auth.exception.AuthException;

import java.util.List;

public interface JWTAuthInterface {
    String createToken(List<JwtClaimDTO> claims) throws AuthException;
    boolean validateToken(String token);
}
