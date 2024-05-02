package com.app.config;

import com.app.infrastructure.interceptor.AuthenticationInterceptor;
import com.app.infrastructure.interceptor.AuthorizationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    @Autowired
    private AuthenticationInterceptor authInterceptor;

    @Autowired
    private AuthorizationInterceptor authorizationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        // .addPathPatterns("/foo/bar") could be used to apply interceptors only in specific routes
        // or .excludePathPatterns("/foo/bar") to exclude. both methods accept wildcards (*/**)

        registry.addInterceptor(authInterceptor).excludePathPatterns("/auth/login");
        registry.addInterceptor(authorizationInterceptor).excludePathPatterns("/auth/**");
    }
}