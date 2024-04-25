package com.app.application.util;

import com.app.application.dto.auth.JwtClaimDTO;
import com.app.application.exception.UnauthenticatedException;
import com.app.infrastructure.cache.CacheInterface;
import com.app.infrastructure.security.auth.JWTAuthInterface;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class AuthInterceptorHandler {
    private final JWTAuthInterface jwtHandler;
    private final CacheInterface cache;

    public AuthInterceptorHandler(JWTAuthInterface jwtHandler, CacheInterface cache) {
        this.jwtHandler = jwtHandler;
        this.cache = cache;
    }

    public void handle(String authHeader) throws UnauthenticatedException {
        this.validateToken(authHeader);

        this.checkTokenInBlackList(authHeader);

        this.checkTokenExpiration(authHeader);

        // TODO
        // criar Session com o usuário autenticado
    }

    private void validateToken(String authHeader) {
        if (authHeader == null || authHeader.isEmpty() || authHeader.isBlank()) {
             throw new UnauthenticatedException("Auth header is empty.");
        }

        String authToken = authHeader.replace("Bearer ", "");

        boolean tokenIsValid = jwtHandler.validateToken(authToken);

        if (!tokenIsValid) {
            throw new UnauthenticatedException("Token has invalid content.");
        }
    }

    private void checkTokenInBlackList(String authToken) {
        Set<String> blackListedTokens = cache.getList("auth_tokens_blacklist");

        if (blackListedTokens.contains(authToken)) {
            throw new UnauthenticatedException("Provided token is blacklisted.");
        }
    }

    private void checkTokenExpiration(String authToken) {
        List<JwtClaimDTO> claims = jwtHandler.getClaims();
        String expiresAt = null;

        for (JwtClaimDTO claimDTO : claims) {
            if ("expiresAt".equals(claimDTO.key()) && !claimDTO.value().isEmpty()) {
                expiresAt = claimDTO.value();
            }
        }

        if (expiresAt == null) {
            throw new UnauthenticatedException("Token's expiration not set");
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
        Date currentDate = new Date();
        String currentDateString = dateFormat.format(currentDate);

        Date currentDateTime = null;
        Date expiresAtDate = null;

        try {
            currentDateTime = dateFormat.parse(currentDateString);
            expiresAtDate = dateFormat.parse(expiresAt);
        } catch (ParseException exception) {
            throw new UnauthenticatedException("Date parsing failed.");
        }

        if (currentDateTime.after(expiresAtDate)) {
            throw new UnauthenticatedException("Provided token is expired.");
        }
    }
}
