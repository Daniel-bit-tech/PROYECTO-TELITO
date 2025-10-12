package com.example.telitodev.interceptor;

import com.example.telitodev.entity.Usuario;
import com.example.telitodev.service.ImpersonationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class RoleConsistencyInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RoleConsistencyInterceptor.class);

    @Autowired
    private ImpersonationService impersonationService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        HttpSession session = request.getSession(false);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Solo validar rutas específicas de roles
        if (shouldValidateRoute(requestURI)) {
            logger.debug("🔍 Validando consistencia de rol para ruta: {}", requestURI);
            
            if (session != null && authentication != null) {
                // Verificar si hay impersonación activa
                boolean isImpersonating = impersonationService.isImpersonating(session);
                
                if (!isImpersonating) {
                    // No hay impersonación, validar que el usuario real pueda acceder a esta ruta
                    String userRole = getUserRealRole(authentication);
                    String expectedRoleForRoute = getExpectedRoleForRoute(requestURI);
                    
                    logger.debug("🔍 Usuario real: {} | Rol: {} | Ruta esperada para: {}", 
                        authentication.getName(), userRole, expectedRoleForRoute);
                    
                    // Si el usuario es SUPERADMIN intentando acceder a rutas de otros roles SIN impersonación
                    if ("SUPERADMIN".equals(userRole) && !expectedRoleForRoute.equals("SUPERADMIN")) {
                        logger.warn("🚨 VIOLACIÓN DE SEGURIDAD: SUPERADMIN {} intentando acceder a ruta {} sin impersonación", 
                            authentication.getName(), requestURI);
                        
                        // Limpiar cualquier estado residual de sesión
                        cleanSessionState(session);
                        
                        // Redireccionar a admin con parámetro de alerta
                        response.sendRedirect("/admin/gestion-usuarios?securityAlert=true");
                        return false;
                    }
                    
                    // Si el usuario no es SUPERADMIN pero intenta acceder a rutas admin
                    if (!"SUPERADMIN".equals(userRole) && expectedRoleForRoute.equals("SUPERADMIN")) {
                        logger.warn("🚨 VIOLACIÓN DE SEGURIDAD: Usuario {} con rol {} intentando acceder a admin", 
                            authentication.getName(), userRole);
                        
                        // Redireccionar al portal correspondiente
                        String redirectUrl = getRedirectUrlForRole(userRole);
                        response.sendRedirect(redirectUrl);
                        return false;
                    }
                } else {
                    // Hay impersonación activa, validar que la ruta coincida con el rol impersonado
                    String impersonatedRole = (String) session.getAttribute("impersonatedUserRole");
                    String expectedRoleForRoute = getExpectedRoleForRoute(requestURI);
                    
                    logger.debug("🎭 Impersonación activa - Rol impersonado: {} | Ruta esperada para: {}", 
                        impersonatedRole, expectedRoleForRoute);
                    
                    if (impersonatedRole != null && !impersonatedRole.equals(expectedRoleForRoute)) {
                        logger.warn("🚨 INCONSISTENCIA EN IMPERSONACIÓN: Rol impersonado {} no coincide con ruta {}", 
                            impersonatedRole, requestURI);
                        
                        // Redireccionar al portal correcto del rol impersonado
                        String redirectUrl = getRedirectUrlForRole(impersonatedRole);
                        response.sendRedirect(redirectUrl);
                        return false;
                    }
                }
            }
        }
        
        return true;
    }

    private boolean shouldValidateRoute(String requestURI) {
        return requestURI.startsWith("/admin/") || 
               requestURI.startsWith("/qa/") || 
               requestURI.startsWith("/po/") || 
               requestURI.startsWith("/dev/");
    }

    private String getUserRealRole(Authentication authentication) {
        // Aquí puedes implementar la lógica para obtener el rol real del usuario
        // Por ahora, asumimos que si el nombre es "Carlos" es SUPERADMIN
        if ("Carlos".equals(authentication.getName()) || 
            authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"))) {
            return "SUPERADMIN";
        }
        
        // Para otros usuarios, determinar el rol basado en authorities
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

    private String getExpectedRoleForRoute(String requestURI) {
        if (requestURI.startsWith("/admin/")) {
            return "SUPERADMIN";
        } else if (requestURI.startsWith("/qa/")) {
            return "QA";
        } else if (requestURI.startsWith("/po/")) {
            return "PO";
        } else if (requestURI.startsWith("/dev/")) {
            return "DEVELOPER";
        }
        return "UNKNOWN";
    }

    private String getRedirectUrlForRole(String role) {
        switch (role) {
            case "SUPERADMIN":
                return "/admin/home";
            case "QA":
                return "/qa/home";
            case "PO":
                return "/po/home";
            case "DEVELOPER":
                return "/dev/home";
            default:
                return "/login";
        }
    }

    private void cleanSessionState(HttpSession session) {
        // Limpiar cualquier estado residual de impersonación
        session.removeAttribute("isImpersonating");
        session.removeAttribute("impersonatedUserId");
        session.removeAttribute("impersonatedUserDni");
        session.removeAttribute("impersonatedUserName");
        session.removeAttribute("impersonatedUserRole");
        
        logger.debug("🧹 Estado de sesión limpiado");
    }
}