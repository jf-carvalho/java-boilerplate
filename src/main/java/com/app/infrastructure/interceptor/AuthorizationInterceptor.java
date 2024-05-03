package com.app.infrastructure.interceptor;

import com.app.application.util.authorization.AuthorizationInterceptorHandler;
import com.app.application.util.authorization.RequiresAuthorization;
import com.app.application.util.http.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

public class AuthorizationInterceptor implements HandlerInterceptor {
    private final AuthorizationInterceptorHandler handler;

    public AuthorizationInterceptor(AuthorizationInterceptorHandler handler) {
        this.handler = handler;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod handlerMethod) {
            RequiresAuthorization annotation = handlerMethod.getMethodAnnotation(RequiresAuthorization.class);

            if (annotation == null) {
                return true;
            }

            String actionName = annotation.value();

            boolean authorized = this.handler.handle(actionName);

            if (!authorized) {
                return this.unauthorized(response);
            }
        }

        return true;
    }

    private boolean unauthorized(HttpServletResponse response) throws IOException {
        ResponseEntity<?> unauthorizedResponse = new ResponseEntity<>(new ErrorResponse("Unauthorized"), HttpStatus.FORBIDDEN);
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setHeader("Content-Type", "application/json");
        response.getWriter().write(new ObjectMapper().writeValueAsString(unauthorizedResponse.getBody()));
        return false;
    }
}
