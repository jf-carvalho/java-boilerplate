package com.app.infrastructure.security.auth;

import com.app.domain.entity.User;

public interface AuthHolderInterface {
    void setAuth(User authUser, String token);
    AuthHolderInterface getAuth();
    User getUser();
    String getToken();
}
