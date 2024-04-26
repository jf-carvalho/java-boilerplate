package com.app.infrastructure.security.auth;

import com.app.domain.entity.User;
import com.app.infrastructure.security.auth.exception.AuthException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SpringAuthHolder implements AuthHolderInterface {
    private Authentication auth;

    @Override
    public void setAuth(User authUser, String token) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                authUser,
                token
        );

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    public Authentication getAuth() {

        return SecurityContextHolder.getContext().getAuthentication();
    }

    @Override
    public User getUser() {
        Authentication auth = this.getAuth();

        if (auth == null) {
            throw new AuthException("There is no authenticated user");
        }

        return (User) auth.getPrincipal();
    }

    @Override
    public String getToken() {
        Authentication auth = this.getAuth();

        if (auth == null) {
            throw new AuthException("There is no authenticated user");
        }

        return (String) auth.getCredentials();
    }


}
