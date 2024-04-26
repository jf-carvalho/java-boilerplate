package com.app.infrastructure.security.auth;

import com.app.domain.entity.User;
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

    @Override
    public AuthHolderInterface getAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) {
            return null;
        }

        this.auth = auth;
        return this;
    }

    @Override
    public User getUser() {
        return (User) this.auth.getPrincipal();
    }

    @Override
    public String getToken() {
        return (String) this.auth.getCredentials();
    }


}
