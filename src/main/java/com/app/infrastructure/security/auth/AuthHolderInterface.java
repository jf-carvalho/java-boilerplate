package com.app.infrastructure.security.auth;

import com.app.domain.entity.User;
import com.app.infrastructure.security.auth.exception.AuthException;

public interface AuthHolderInterface {
    void setAuth(User authUser, String token);
    User getUser() throws AuthException;
    String getToken();
}
