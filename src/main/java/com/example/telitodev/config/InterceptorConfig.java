package com.example.telitodev.config;

import com.example.telitodev.interceptor.RoleConsistencyInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private RoleConsistencyInterceptor roleConsistencyInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(roleConsistencyInterceptor)
                .addPathPatterns("/admin/**", "/qa/**", "/po/**", "/dev/**")
                .excludePathPatterns(
                    "/admin/gestion-usuarios/stop-impersonation",
                    "/admin/gestion-usuarios/emergency-cleanup",
                    "/admin/gestion-usuarios/impersonation-status",
                    "/api/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/static/**"
                );
    }
}