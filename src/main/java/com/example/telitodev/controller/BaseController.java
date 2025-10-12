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
            String impersonatedUserRole = (String) session.getAttribute("IMPERSONATED_USER_ROLE");
            String impersonatedUserEmail = (String) session.getAttribute("IMPERSONATED_USER_EMAIL");
            
            System.out.println("  - impersonatedUserDni: " + impersonatedUserDni);
            System.out.println("  - impersonatedUserName: " + impersonatedUserName);
            System.out.println("  - impersonatedUserRole: " + impersonatedUserRole);
            System.out.println("  - impersonatedUserEmail: " + impersonatedUserEmail);
            
            model.addAttribute("impersonatedUserDni", impersonatedUserDni);
            model.addAttribute("impersonatedUserName", impersonatedUserName);
            model.addAttribute("impersonatedUserRole", impersonatedUserRole);
            model.addAttribute("impersonatedUserEmail", impersonatedUserEmail);
        } else {
            System.out.println("  - No hay impersonación activa");
        }
    }

    /**
     * Valida que el usuario actual tenga permisos para acceder al rol especificado
     * Previene acceso de SUPERADMIN a vistas de otros roles sin impersonación
     */
    protected boolean validateRoleAccess(Authentication authentication, HttpSession session, String expectedRole) {
        if (authentication == null) {
            return false;
        }

        boolean isImpersonating = impersonationService.isImpersonating(session);
        String userName = authentication.getName();

        System.out.println("🔒 VALIDACIÓN DE ROL:");
        System.out.println("  - Usuario: " + userName);
        System.out.println("  - Rol esperado: " + expectedRole);
        System.out.println("  - ¿Impersonando?: " + isImpersonating);

        if (isImpersonating) {
            // Si está impersonando, validar que el rol impersonado coincida
            String impersonatedRole = (String) session.getAttribute("IMPERSONATED_USER_ROLE");
            System.out.println("  - Rol impersonado: " + impersonatedRole);
            return expectedRole.equals(impersonatedRole);
        } else {
            // Si NO está impersonando, validar que no sea SUPERADMIN accediendo a otros roles
            boolean isCarlos = "Carlos".equals(userName);
            boolean hasSuperAdminRole = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));

            if ((isCarlos || hasSuperAdminRole) && !"SUPERADMIN".equals(expectedRole)) {
                System.out.println("  - ❌ SUPERADMIN intentando acceder a rol " + expectedRole + " sin impersonación");
                return false;
            }

            // Para otros usuarios, verificar que tengan el rol correcto
            String userRole = getUserRoleFromAuthentication(authentication);
            System.out.println("  - Rol del usuario: " + userRole);
            return expectedRole.equals(userRole);
        }
    }

    /**
     * Obtiene el rol del usuario desde la autenticación
     */
    protected String getUserRoleFromAuthentication(Authentication authentication) {
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"))) {
            return "SUPERADMIN";
        }
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_QA"))) {
            return "QA";
        }
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PO"))) {
            return "PO";
        }
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DEVELOPER"))) {
            return "DEVELOPER";
        }
        return "UNKNOWN";
    }

    /**
     * Obtiene la URL de redirección apropiada para un rol
     */
    protected String getRedirectUrlForRole(String role) {
        switch (role) {
            case "SUPERADMIN":
                return "redirect:/admin/home";
            case "QA":
                return "redirect:/qa/home";
            case "PO":
                return "redirect:/po/home";
            case "DEVELOPER":
                return "redirect:/dev/home";
            default:
                return "redirect:/login";
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