package com.example.telitodev.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador para limpiar completamente la sesión OAuth2
 */
@Controller
public class OAuth2ResetController {

    @GetMapping("/oauth2-reset")
    public String resetOAuth2Session(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("🔄 OAUTH2 RESET - LIMPIANDO SESIÓN COMPLETA");
        
        // Obtener la autenticación actual
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null) {
            System.out.println("   - Usuario actual: " + auth.getName());
            System.out.println("   - Autoridades: " + auth.getAuthorities());
            
            // Limpiar el contexto de seguridad completamente
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        
        // Invalidar la sesión HTTP
        if (request.getSession(false) != null) {
            System.out.println("   - Invalidando sesión: " + request.getSession().getId());
            request.getSession().invalidate();
        }
        
        System.out.println("✅ OAUTH2 RESET - SESIÓN LIMPIADA");
        
        // Redirigir a login limpio
        return "redirect:/login?reset=true";
    }
}