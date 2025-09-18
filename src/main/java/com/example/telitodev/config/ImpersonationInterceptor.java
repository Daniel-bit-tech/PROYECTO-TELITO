package com.example.telitodev.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class ImpersonationInterceptor implements HandlerInterceptor {

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, 
                          Object handler, ModelAndView modelAndView) throws Exception {
        
        if (modelAndView != null) {
            HttpSession session = request.getSession(false);
            
            if (session != null) {
                // Verificar si hay impersonación activa
                Boolean isImpersonating = (Boolean) session.getAttribute("IS_IMPERSONATING");
                String originalAdmin = (String) session.getAttribute("ORIGINAL_ADMIN_USERNAME");
                String impersonatedUserDni = (String) session.getAttribute("IMPERSONATED_USER_DNI");
                
                // Agregar al modelo para que Thymeleaf pueda acceder
                modelAndView.addObject("isImpersonating", isImpersonating != null && isImpersonating);
                modelAndView.addObject("originalAdminUsername", originalAdmin);
                modelAndView.addObject("impersonatedUserDni", impersonatedUserDni);
                
                System.out.println("=== IMPERSONATION INTERCEPTOR ===");
                System.out.println("URL: " + request.getRequestURI());
                System.out.println("Is Impersonating: " + isImpersonating);
                System.out.println("Original Admin: " + originalAdmin);
                System.out.println("Impersonated DNI: " + impersonatedUserDni);
            }
        }
    }
}