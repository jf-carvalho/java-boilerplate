package com.app.infrastructure.security.auth;

import com.app.application.dto.auth.JwtClaimDTO;
import com.app.infrastructure.security.auth.exception.AuthException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Auth0JWTHandler implements JWTAuthInterface {
    private final Algorithm algorithm;
    private DecodedJWT decodedJWT;

    public Auth0JWTHandler(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    @Override
    public String createToken(List<JwtClaimDTO> claims) {
        try {
            JWTCreator.Builder token = JWT.create()
                    .withIssuer("auth0");

            claims.forEach(claim -> token.withClaim(claim.key(), claim.value()));

            return token.sign(this.algorithm);
        } catch (JWTCreationException | IllegalArgumentException exception) {
            throw new AuthException("JWT token creation failed.");
        }
    }

    @Override
    public boolean validateToken(String token) {
        DecodedJWT decodedJWT;

        try {
            JWTVerifier verifier = JWT.require(this.algorithm)
                    .withIssuer("auth0")
                    .build();

            decodedJWT = verifier.verify(token);
            this.decodedJWT = decodedJWT;
        } catch (JWTVerificationException exception){
            throw new AuthException("Token validation failed");
        }

        return true;
    }

    public List<JwtClaimDTO> getClaims() {
        if (this.decodedJWT == null) {
            throw new AuthException("JWT must be decoded in order to get it's claims.");
        }

        Map<String, Claim> claims = this.decodedJWT.getClaims();

        List<JwtClaimDTO> claimsDTOs = new ArrayList<>();

        for (Map.Entry<String, Claim> entry : claims.entrySet()) {
            String claimName = entry.getKey();
            Claim claimValue = entry.getValue();

            JwtClaimDTO claimDTO = new JwtClaimDTO(claimName, claimValue.asString());

            claimsDTOs.add(claimDTO);
        }

        return claimsDTOs;
    }
}
