package com.example.telitodev.controller;

import com.example.telitodev.entity.Usuario;
import com.example.telitodev.service.ImpersonationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

/**
 * Controlador base que proporciona funcionalidad común para todos los controladores,
 * especialmente el manejo de impersonación
 */
public abstract class BaseController {

    @Autowired
    protected ImpersonationService impersonationService;

    /**
     * Agrega automáticamente las variables de impersonación al modelo
     * para que todas las plantillas puedan mostrar el banner de impersonación
     */
    protected void addImpersonationAttributes(Model model, HttpSession session) {
        boolean isImpersonating = impersonationService.isImpersonating(session);
        
        // Debug: Agregar logs para verificar el estado de impersonación
        System.out.println("🎭 DEBUG - addImpersonationAttributes:");
        System.out.println("  - isImpersonating: " + isImpersonating);
        System.out.println("  - Session ID: " + (session != null ? session.getId() : "null"));
        
        model.addAttribute("isImpersonating", isImpersonating);
        
        if (isImpersonating) {
            String impersonatedUserDni = impersonationService.getImpersonatedUserDni(session);
            String impersonatedUserName = impersonationService.getImpersonatedUserName(session);
            
            System.out.println("  - impersonatedUserDni: " + impersonatedUserDni);
            System.out.println("  - impersonatedUserName: " + impersonatedUserName);
            
            model.addAttribute("impersonatedUserDni", impersonatedUserDni);
            model.addAttribute("impersonatedUserName", impersonatedUserName);
        } else {
            System.out.println("  - No hay impersonación activa");
        }
    }

    /**
     * Obtiene el usuario correcto considerando la impersonación
     * Si hay impersonación activa, devuelve el usuario impersonado
     * Si no hay impersonación, devuelve el usuario autenticado
     */
    protected Usuario getCurrentUser(Authentication auth, HttpSession session) {
        return impersonationService.getCurrentUser(auth, session);
    }

    /**
     * Verifica si hay una impersonación activa
     */
    protected boolean isImpersonating(HttpSession session) {
        return impersonationService.isImpersonating(session);
    }
}