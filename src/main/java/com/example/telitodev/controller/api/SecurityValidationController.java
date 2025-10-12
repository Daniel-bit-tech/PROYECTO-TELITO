package com.example.telitodev.controller.api;

import com.example.telitodev.service.ImpersonationService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SecurityValidationController {

    private static final Logger logger = LoggerFactory.getLogger(SecurityValidationController.class);

    @Autowired
    private ImpersonationService impersonationService;

    @GetMapping("/validate-session")
    public ResponseEntity<Map<String, Object>> validateSession(Authentication authentication, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authentication == null) {
                response.put("valid", false);
                response.put("error", "No authentication found");
                return ResponseEntity.status(401).body(response);
            }

            String userName = authentication.getName();
            boolean isImpersonating = impersonationService.isImpersonating(session);
            
            logger.debug("🔍 Validando sesión para usuario: {} | Impersonando: {}", userName, isImpersonating);
            
            // Determinar el rol real del usuario
            String actualRole = getUserRoleFromAuthentication(authentication);
            
            response.put("valid", true);
            response.put("userName", userName);
            response.put("actualRole", actualRole);
            response.put("isImpersonating", isImpersonating);
            
            if (isImpersonating) {
                String impersonatedRole = (String) session.getAttribute("IMPERSONATED_USER_ROLE");
                String impersonatedUserName = (String) session.getAttribute("IMPERSONATED_USER_NAME");
                String impersonatedUserDni = (String) session.getAttribute("IMPERSONATED_USER_DNI");
                
                response.put("impersonatedRole", impersonatedRole);
                response.put("impersonatedUserName", impersonatedUserName);
                response.put("impersonatedUserDni", impersonatedUserDni);
                
                logger.debug("🎭 Datos de impersonación - Rol: {} | Usuario: {}", impersonatedRole, impersonatedUserName);
            }
            
            // Detectar violaciones de seguridad
            boolean securityViolation = false;
            String violationMessage = "";
            String redirectUrl = "";
            
            // Si es SUPERADMIN pero no está impersonando, debe estar en admin
            if ("SUPERADMIN".equals(actualRole) && !isImpersonating) {
                // Esto está bien, SUPERADMIN puede acceder a admin sin impersonación
                logger.debug("✅ SUPERADMIN accediendo sin impersonación - válido");
            }
            
            // Si está impersonando, debe tener datos válidos de impersonación
            if (isImpersonating) {
                String impersonatedRole = (String) session.getAttribute("IMPERSONATED_USER_ROLE");
                if (impersonatedRole == null || impersonatedRole.isEmpty()) {
                    securityViolation = true;
                    violationMessage = "Impersonación activa pero sin rol válido";
                    redirectUrl = "/admin/home";
                    
                    logger.warn("🚨 VIOLACIÓN: Impersonación sin rol válido para usuario {}", userName);
                }
            }
            
            response.put("securityViolation", securityViolation);
            if (securityViolation) {
                response.put("message", violationMessage);
                response.put("redirectUrl", redirectUrl);
            }
            
            logger.debug("✅ Validación completada - Violación: {} | Respuesta: {}", securityViolation, response);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Error en validación de sesión", e);
            response.put("valid", false);
            response.put("error", "Error interno del servidor");
            return ResponseEntity.status(500).body(response);
        }
    }

    private String getUserRoleFromAuthentication(Authentication authentication) {
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
            return "DESARROLLADOR";
        }
        
        // Fallback basado en nombre de usuario (para casos especiales)
        if ("Carlos".equals(authentication.getName())) {
            return "SUPERADMIN";
        }
        
        return "UNKNOWN";
    }
}