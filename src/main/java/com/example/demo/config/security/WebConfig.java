package com.example.demo.config.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AdminInterceptor adminInterceptor;
    private final UserInterceptor userInterceptor;
    private final RateLimitInterceptor rateLimitInterceptor;

    public WebConfig(AdminInterceptor adminInterceptor, UserInterceptor userInterceptor, RateLimitInterceptor rateLimitInterceptor) {
        this.adminInterceptor = adminInterceptor;
        this.userInterceptor = userInterceptor;
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/**");

        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**");
                          
        registry.addInterceptor(userInterceptor)
                .addPathPatterns("/dashboard/**");
    }
}