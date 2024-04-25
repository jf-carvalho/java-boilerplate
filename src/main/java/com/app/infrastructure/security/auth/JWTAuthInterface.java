package com.app.infrastructure.security.auth;

import com.app.application.dto.auth.JwtClaimDTO;
import com.app.infrastructure.security.auth.exception.AuthException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.List;

public interface JWTAuthInterface {
    String createToken(List<JwtClaimDTO> claims) throws AuthException;
    boolean validateToken(String token);
    List<JwtClaimDTO> getClaims();
}
