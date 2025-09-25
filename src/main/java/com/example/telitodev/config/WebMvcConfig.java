package com.example.telitodev.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private ImpersonationInterceptor impersonationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(impersonationInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/api/**", "/static/**", "/css/**", "/js/**", "/images/**");
    }
}