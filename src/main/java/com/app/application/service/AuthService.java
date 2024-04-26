package com.app.application.service;

import com.app.application.dto.auth.JwtClaimDTO;
import com.app.application.dto.auth.LoginRequestDTO;
import com.app.application.dto.auth.LoginResponseDTO;
import com.app.application.dto.user.UserResponseWithPasswordDTO;
import com.app.application.exception.ResourceNotFound;
import com.app.application.exception.UnauthenticatedException;
import com.app.infrastructure.cache.CacheInterface;
import com.app.infrastructure.security.auth.AuthHolderInterface;
import com.app.infrastructure.security.auth.JWTAuthInterface;
import com.app.infrastructure.security.hasher.HasherInterface;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AuthService {
    private final JWTAuthInterface auth;
    private final UserService userService;
    private final HasherInterface hasher;
    private final CacheInterface cache;
    private final AuthHolderInterface authHolder;

    public AuthService(
            JWTAuthInterface auth,
            UserService userService,
            HasherInterface hasher,
            CacheInterface cache,
            AuthHolderInterface authHolder
    ) {
        this.auth = auth;
        this.userService = userService;
        this.hasher = hasher;
        this.cache = cache;
        this.authHolder = authHolder;
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

        if (currentUserToken != null && !currentUserToken.isEmpty()) {
            this.cache.add("auth_tokens_blacklist", currentUserToken);
        }

        List<JwtClaimDTO> claims = this.getClaims(user);

        String token = auth.createToken(claims);

        this.cache.set(user.id() + "_current_token", token);

        return new LoginResponseDTO(token);
    }

    private List<JwtClaimDTO> getClaims(UserResponseWithPasswordDTO user) {
        List<JwtClaimDTO> claims = new ArrayList<>();
        claims.add(new JwtClaimDTO("userId", user.id().toString()));

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

    public boolean logout() {
        String authToken = this.authHolder.getAuth().getToken();

        this.cache.add("auth_tokens_blacklist", authToken);

        return true;
    }
}