package com.app.config;

import com.app.infrastructure.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        ServiceContainer container = new ServiceContainer();
        // .addPathPatterns("/foo/bar") could be used to apply interceptors only in specific routes
        // or .excludePathPatterns("/foo/bar") to exclude. both methods accept wildcards (*/**)

        registry.addInterceptor(new AuthInterceptor(container.authInterceptorHandler())).excludePathPatterns("/auth/**");
    }
}