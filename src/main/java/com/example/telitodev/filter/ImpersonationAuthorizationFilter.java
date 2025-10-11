package com.example.telitodev.filter;

import com.example.telitodev.service.ImpersonationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;

@Component
public class ImpersonationAuthorizationFilter extends OncePerRequestFilter {

    @Autowired
    private ImpersonationService impersonationService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        
        // Excluir rutas de gestión de impersonación
        if (requestURI.contains("/stop-impersonation") || requestURI.contains("/start-impersonation")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Solo procesar rutas de portales
        if (requestURI.startsWith("/admin/") || requestURI.startsWith("/dev/") || 
            requestURI.startsWith("/qa/") || requestURI.startsWith("/po/")) {
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                
                // Verificar si hay impersonación activa
                boolean isImpersonating = impersonationService.isImpersonating(request.getSession());
                
                if (isImpersonating) {
                    // SEGURIDAD: Si hay impersonación activa, NO permitir acceso a /admin/
                    if (requestURI.startsWith("/admin/")) {
                        System.out.println("🚫 ACCESO DENEGADO: Intento de acceder a /admin/ durante impersonación");
                        System.out.println("   URI solicitada: " + requestURI);
                        
                        // Redirigir al portal del usuario impersonado
                        String impersonatedRole = impersonationService.getImpersonatedUserRole(request.getSession());
                        String redirectUrl = getRedirectUrlForRole(impersonatedRole);
                        System.out.println("   Redirigiendo a: " + redirectUrl);
                        response.sendRedirect(redirectUrl);
                        return;
                    }
                    
                    // Si hay impersonación, verificar el rol impersonado para otros portales
                    String impersonatedRole = impersonationService.getImpersonatedUserRole(request.getSession());
                    if (!hasAccessToPath(requestURI, impersonatedRole)) {
                        // Redirigir al portal correcto basado en el rol impersonado
                        String redirectUrl = getRedirectUrlForRole(impersonatedRole);
                        response.sendRedirect(redirectUrl);
                        return;
                    }
                } else {
                    // Si no hay impersonación, verificar el rol real del usuario
                    Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
                    if (!hasAccessToPath(requestURI, authorities)) {
                        // Redirigir al portal correcto basado en el rol real
                        String redirectUrl = getRedirectUrlForAuthorities(authorities);
                        response.sendRedirect(redirectUrl);
                        return;
                    }
                }
            }
        }
        
        filterChain.doFilter(request, response);
    }

    private boolean hasAccessToPath(String path, String role) {
        if (path.startsWith("/admin/")) {
            return "SUPERADMIN".equals(role);
        } else if (path.startsWith("/dev/")) {
            return "DEV".equals(role) || "SUPERADMIN".equals(role);
        } else if (path.startsWith("/qa/")) {
            return "QA".equals(role) || "SUPERADMIN".equals(role);
        } else if (path.startsWith("/po/")) {
            return "PO".equals(role) || "SUPERADMIN".equals(role);
        }
        return false;
    }

    private boolean hasAccessToPath(String path, Collection<? extends GrantedAuthority> authorities) {
        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority().replace("ROLE_", "");
            if (hasAccessToPath(path, role)) {
                return true;
            }
        }
        return false;
    }

    private String getRedirectUrlForRole(String role) {
        switch (role) {
            case "SUPERADMIN":
                return "/admin/home";
            case "DEV":
                return "/dev/home";
            case "QA":
                return "/qa/home";
            case "PO":
                return "/po/home";
            default:
                return "/login";
        }
    }

    private String getRedirectUrlForAuthorities(Collection<? extends GrantedAuthority> authorities) {
        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority().replace("ROLE_", "");
            String redirectUrl = getRedirectUrlForRole(role);
            if (!"/login".equals(redirectUrl)) {
                return redirectUrl;
            }
        }
        return "/login";
    }
}