package com.example.telitodev.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class OAuth2TestController {

    /**
     * Endpoint para limpiar completamente la sesión OAuth2 y forzar nueva autenticación
     */
    @GetMapping("/oauth2/reset")
    @ResponseBody
    public String resetOAuth2Session(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("🔄 LIMPIANDO SESIÓN OAUTH2 PARA FORZAR NUEVA AUTENTICACIÓN");
        
        // Limpiar contexto de seguridad
        SecurityContextHolder.clearContext();
        
        // Limpiar sesión
        if (request.getSession(false) != null) {
            System.out.println("   - Invalidando sesión: " + request.getSession().getId());
            request.getSession().invalidate();
        }
        
        // Forzar logout completo
        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        
        System.out.println("   ✅ Sesión limpiada. Redirigir a /login para nueva autenticación");
        
        return "<!DOCTYPE html><html><head><title>OAuth2 Reset</title></head><body>" +
               "<h2>🔄 Sesión OAuth2 limpiada</h2>" +
               "<p>Ahora puedes hacer login con Google para probar el OAuth2UserService personalizado.</p>" +
               "<a href='/login' style='background: #4285f4; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>🔐 Ir a Login</a>" +
               "</body></html>";
    }
    
    /**
     * Endpoint para debugging OAuth2
     */
    @GetMapping("/oauth2/debug")
    @ResponseBody
    public String debugOAuth2(HttpServletRequest request) {
        StringBuilder debug = new StringBuilder();
        debug.append("🔍 DEBUG OAUTH2 SESSION:<br>");
        debug.append("- Session ID: ").append(request.getSession(false) != null ? request.getSession().getId() : "NULL").append("<br>");
        debug.append("- Authentication: ").append(SecurityContextHolder.getContext().getAuthentication()).append("<br>");
        debug.append("- Principal: ").append(SecurityContextHolder.getContext().getAuthentication() != null ? 
                    SecurityContextHolder.getContext().getAuthentication().getName() : "NULL").append("<br>");
        
        return debug.toString();
    }
}