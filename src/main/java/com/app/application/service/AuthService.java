package com.app.application.service;

import com.app.application.dto.auth.JwtClaimDTO;
import com.app.application.dto.auth.LoginRequestDTO;
import com.app.application.dto.auth.LoginResponseDTO;
import com.app.application.dto.auth.RefreshAuthRequestDTO;
import com.app.application.dto.user.UserResponseWithPasswordDTO;
import com.app.application.exception.ResourceNotFound;
import com.app.application.exception.UnauthenticatedException;
import com.app.domain.entity.User;
import com.app.infrastructure.cache.CacheInterface;
import com.app.infrastructure.security.auth.AuthHolderInterface;
import com.app.infrastructure.security.auth.JWTAuthInterface;
import com.app.infrastructure.security.hasher.HasherInterface;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AuthService {
    private final JWTAuthInterface auth;
    private final UserService userService;
    private final HasherInterface hasher;
    private final CacheInterface cache;
    private final AuthHolderInterface authHolder;
    private final JWTAuthInterface jwtHandler;

    public AuthService(
            JWTAuthInterface auth,
            UserService userService,
            HasherInterface hasher,
            CacheInterface cache,
            AuthHolderInterface authHolder,
            JWTAuthInterface jwtHandler
    ) {
        this.auth = auth;
        this.userService = userService;
        this.hasher = hasher;
        this.cache = cache;
        this.authHolder = authHolder;
        this.jwtHandler = jwtHandler;
    }

    public LoginResponseDTO attemptLogin(LoginRequestDTO loginRequestDTO) throws ResourceNotFound {
        UserResponseWithPasswordDTO user = null;

        try {
            user = this.userService.getUserForLogin(loginRequestDTO.email());
        } catch (ResourceNotFound exception) {
            throw new UnauthenticatedException("Wrong credentials.");
        }

        boolean passwordMatches = hasher.checkHash(user.password(), loginRequestDTO.password());

        if (!passwordMatches) {
            throw new UnauthenticatedException("Wrong credentials.");
        }

        String currentUserToken = this.cache.get(user.id().toString() + "_current_token");

        if (currentUserToken != null) {
            this.cache.add("auth_tokens_blacklist", currentUserToken);
        }

        return this.generateTokens(user.id());
    }

    public LoginResponseDTO refreshToken(RefreshAuthRequestDTO refreshAuthRequestDTO) {
        User loggedUser = this.authHolder.getUser();

        String cachedRefreshToken = this.cache.get(loggedUser.getId() + "_refresh_token");

        if (cachedRefreshToken == null) {
            throw new UnauthenticatedException("User does not have a refresh token stored.");
        }

        if (!Objects.equals(refreshAuthRequestDTO.refreshToken(), cachedRefreshToken)) {
            throw new UnauthenticatedException("Invalid refresh token.");
        }

        this.checkRefreshTokenExpiration(refreshAuthRequestDTO.refreshToken());

        String currentUserToken = this.cache.get(loggedUser.getId().toString() + "_current_token");

        this.cache.add("auth_tokens_blacklist", currentUserToken);

        return this.generateTokens(loggedUser.getId());
    }

    private LoginResponseDTO generateTokens(Long userId) {
        List<JwtClaimDTO> accessClaims = this.getAccessClaims(userId);
        String accessToken = auth.createToken(accessClaims);
        this.cache.set(userId + "_current_token", accessToken);

        List<JwtClaimDTO> refreshClaims = this.getRefreshClaims(userId);
        String refreshToken = auth.createToken(refreshClaims);
        this.cache.set(userId + "_refresh_token", refreshToken);

        return new LoginResponseDTO(accessToken, refreshToken);
    }

    private void checkRefreshTokenExpiration(String refreshToken) {
        jwtHandler.validateToken(refreshToken);
        List<JwtClaimDTO> claims = jwtHandler.getClaims();

        String expiresAt = null;

        for (JwtClaimDTO claimDTO : claims) {
            if ("expiresAt".equals(claimDTO.key()) && !claimDTO.value().isEmpty()) {
                expiresAt = claimDTO.value();
            }
        }

        if (expiresAt == null) {
            throw new UnauthenticatedException("Token's expiration not set.");
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

    private List<JwtClaimDTO> getAccessClaims(Long userId) {
        List<JwtClaimDTO> claims = new ArrayList<>();
        claims.add(new JwtClaimDTO("userId", userId.toString()));

        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
        String formattedDate = dateFormat.format(currentDate);

        claims.add(new JwtClaimDTO("createdAt", formattedDate));

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);

        calendar.add(Calendar.HOUR_OF_DAY, 1);

        Date expirationDate = calendar.getTime();
        String formattedExpirationDate = dateFormat.format(expirationDate);

        claims.add(new JwtClaimDTO("expiresAt", formattedExpirationDate));

        return claims;
    }

    private List<JwtClaimDTO> getRefreshClaims(Long userId) {
        List<JwtClaimDTO> claims = new ArrayList<>();
        claims.add(new JwtClaimDTO("userId", userId.toString()));

        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
        String formattedDate = dateFormat.format(currentDate);

        claims.add(new JwtClaimDTO("createdAt", formattedDate));

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);

        calendar.add(Calendar.HOUR_OF_DAY, 4);

        Date expirationDate = calendar.getTime();
        String formattedExpirationDate = dateFormat.format(expirationDate);

        claims.add(new JwtClaimDTO("expiresAt", formattedExpirationDate));
        claims.add(new JwtClaimDTO("type", "refresh"));

        return claims;
    }

    public boolean logout() {
        String authToken = this.authHolder.getToken();

        this.cache.add("auth_tokens_blacklist", authToken);

        return true;
    }
}