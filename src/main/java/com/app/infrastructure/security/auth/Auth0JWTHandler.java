package com.app.infrastructure.security.auth;

import com.app.infrastructure.security.auth.exception.AuthException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Autowired;

public class Auth0JWTHandler implements JWTAuthInterface {
    @Autowired
    private final Algorithm algorithm;

    public Auth0JWTHandler(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    @Override
    public String createToken() {
        try {
            return JWT.create()
                    .withIssuer("auth0")
                    .sign(this.algorithm);
        } catch (JWTCreationException | IllegalArgumentException exception) {
            throw new AuthException("JWT token creation failed.");
        }
    }

    @Override
    public boolean validateToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(this.algorithm)
                    .withIssuer("auth0")
                    .build();

            verifier.verify(token);
        } catch (JWTVerificationException exception){
            throw new AuthException("Token validation failed");
        }

        return true;
    }
}
