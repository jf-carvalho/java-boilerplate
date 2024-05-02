package com.app.infrastructure.interceptor;

import com.app.application.exception.UnauthenticatedException;
import com.app.application.util.authentication.AuthInterceptorHandler;
import com.app.application.util.http.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

public class AuthenticationInterceptor implements HandlerInterceptor {
    private final AuthInterceptorHandler handler;

    public AuthenticationInterceptor(AuthInterceptorHandler handler) {
        this.handler = handler;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            String authTokenHeader = request.getHeader("Authorization");

            this.handler.handle(authTokenHeader);
        } catch(UnauthenticatedException exception) {
            return this.unauthenticated(response);
        }

        return true;
    }

    private boolean unauthenticated(HttpServletResponse response) throws IOException {
        ResponseEntity<?> unauthorizedResponse = new ResponseEntity<>(new ErrorResponse("Unauthenticated"), HttpStatus.UNAUTHORIZED);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setHeader("Content-Type", "application/json");
        response.getWriter().write(new ObjectMapper().writeValueAsString(unauthorizedResponse.getBody()));
        return false;
    }

    private boolean serverError(HttpServletResponse response) throws IOException {
        ResponseEntity<?> unauthorizedResponse = new ResponseEntity<>(new ErrorResponse("Server error"), HttpStatus.INTERNAL_SERVER_ERROR);
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setHeader("Content-Type", "application/json");
        response.getWriter().write(new ObjectMapper().writeValueAsString(unauthorizedResponse.getBody()));
        return false;
    }
}
