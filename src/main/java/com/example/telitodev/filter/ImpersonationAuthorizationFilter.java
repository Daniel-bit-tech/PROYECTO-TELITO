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
        
        // Agregar headers de seguridad para prevenir cache de páginas sensibles
        if (requestURI.startsWith("/admin/") || requestURI.startsWith("/dev/") || 
            requestURI.startsWith("/qa/") || requestURI.startsWith("/po/")) {
            
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
        }
        
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
                
                // VERIFICACIÓN CRÍTICA: Si se acaba de terminar impersonación
                Boolean impersonationEnded = (Boolean) request.getSession().getAttribute("IMPERSONATION_ENDED");
                Boolean forceAdminAccess = (Boolean) request.getSession().getAttribute("FORCE_ADMIN_ACCESS");
                
                if (impersonationEnded != null && impersonationEnded && 
                    forceAdminAccess != null && forceAdminAccess) {
                    
                    System.out.println("🔄 IMPERSONACIÓN RECIÉN TERMINADA - Forzando acceso de admin");
                    System.out.println("   URI solicitada: " + requestURI);
                    System.out.println("   Usuario: " + auth.getName());
                    
                    // Limpiar flags una sola vez
                    request.getSession().removeAttribute("IMPERSONATION_ENDED");
                    request.getSession().removeAttribute("FORCE_ADMIN_ACCESS");
                    
                    // Si está intentando acceder a algo que NO es admin, redirigir a admin
                    if (!requestURI.startsWith("/admin/")) {
                        System.out.println("🔄 Redirigiendo a admin tras terminar impersonación");
                        response.sendRedirect("/admin/gestion-usuarios");
                        return;
                    }
                    
                    // Si está accediendo a admin, permitir acceso directo
                    System.out.println("✅ Acceso a admin permitido tras terminar impersonación");
                    filterChain.doFilter(request, response);
                    return;
                }
                
                // Verificar si hay impersonación activa
                boolean isImpersonating = impersonationService.isImpersonating(request.getSession());
                
                System.out.println("🔍 FILTRO - Verificación de impersonación:");
                System.out.println("   URI: " + requestURI);
                System.out.println("   ¿Impersonando?: " + isImpersonating);
                System.out.println("   Usuario autenticado: " + auth.getName());
                
                if (isImpersonating) {
                    // SEGURIDAD: Si hay impersonación activa, NO permitir acceso a /admin/
                    if (requestURI.startsWith("/admin/")) {
                        System.out.println("🚫 ACCESO DENEGADO: Intento de acceder a /admin/ durante impersonación");
                        System.out.println("   URI solicitada: " + requestURI);
                        System.out.println("   Referrer: " + request.getHeader("Referer"));
                        
                        // Configurar headers de seguridad para prevenir cache y navegación hacia atrás
                        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0");
                        response.setHeader("Pragma", "no-cache");
                        response.setHeader("Expires", "0");
                        response.setHeader("X-Frame-Options", "DENY");
                        
                        // Redirigir al portal del usuario impersonado con JavaScript de protección
                        String impersonatedRole = impersonationService.getImpersonatedUserRole(request.getSession());
                        String redirectUrl = getRedirectUrlForRole(impersonatedRole);
                        System.out.println("   Redirigiendo a: " + redirectUrl);
                        
                        // En lugar de un simple redirect, enviar una página con JavaScript de protección
                        sendSecureRedirect(response, redirectUrl, "Durante la impersonación no puedes acceder a páginas de SuperAdmin");
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
                    System.out.println("✅ SIN IMPERSONACIÓN - Verificando rol real del usuario");
                    Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
                    System.out.println("   Authorities: " + authorities);
                    
                    if (!hasAccessToPath(requestURI, authorities)) {
                        System.out.println("🚫 Usuario sin acceso a: " + requestURI);
                        // Redirigir al portal correcto basado en el rol real
                        String redirectUrl = getRedirectUrlForAuthorities(authorities);
                        System.out.println("   Redirigiendo a: " + redirectUrl);
                        response.sendRedirect(redirectUrl);
                        return;
                    } else {
                        System.out.println("✅ Usuario con acceso permitido a: " + requestURI);
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
    
    /**
     * Envía una redirección segura con JavaScript de protección contra navegación hacia atrás
     */
    private void sendSecureRedirect(HttpServletResponse response, String redirectUrl, String message) throws IOException {
        response.setContentType("text/html; charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        
        String html = "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<title>Redirección Segura</title>" +
                "<style>" +
                    "body { " +
                        "font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;" +
                        "background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);" +
                        "margin: 0; padding: 0; height: 100vh;" +
                        "display: flex; align-items: center; justify-content: center;" +
                    "}" +
                    ".container {" +
                        "background: white; padding: 2rem; border-radius: 10px;" +
                        "box-shadow: 0 10px 25px rgba(0,0,0,0.2); text-align: center;" +
                        "max-width: 400px; margin: 0 auto;" +
                    "}" +
                    ".icon { font-size: 3rem; margin-bottom: 1rem; }" +
                    "h1 { color: #333; margin-bottom: 1rem; }" +
                    "p { color: #666; margin-bottom: 1.5rem; }" +
                    ".spinner { " +
                        "width: 40px; height: 40px; margin: 1rem auto;" +
                        "border: 4px solid #f3f3f3; border-top: 4px solid #667eea;" +
                        "border-radius: 50%; animation: spin 1s linear infinite;" +
                    "}" +
                    "@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }" +
                "</style>" +
            "</head>" +
            "<body>" +
                "<div class=\"container\">" +
                    "<div class=\"icon\">🔒</div>" +
                    "<h1>Acceso Restringido</h1>" +
                    "<p>" + message + "</p>" +
                    "<div class=\"spinner\"></div>" +
                    "<p>Redirigiendo automáticamente...</p>" +
                "</div>" +
                
                "<script>" +
                    "console.log('🔒 Protección de impersonación activada');" +
                    
                    "function preventBackNavigation() {" +
                        "const protectionState = { " +
                            "protectedRedirect: true, " +
                            "timestamp: Date.now()," +
                            "reason: 'impersonation_admin_access_blocked'" +
                        "};" +
                        
                        "if (window.history && window.history.replaceState) {" +
                            "window.history.replaceState(protectionState, '', window.location.href);" +
                        "}" +
                        
                        "const handleBackAttempt = (event) => {" +
                            "if (event.state && event.state.protectedRedirect) {" +
                                "console.log('🚫 Navegación hacia atrás bloqueada durante impersonación');" +
                                "alert('⚠️ Durante la impersonación no puedes regresar a páginas de SuperAdmin.\\\\nUsa el botón \"Volver a SuperAdmin\" en la barra superior.');" +
                                "window.history.pushState(protectionState, '', window.location.href);" +
                                "return false;" +
                            "}" +
                        "};" +
                        
                        "window.addEventListener('popstate', handleBackAttempt);" +
                        "window.history.pushState(protectionState, '', window.location.href);" +
                    "}" +
                    
                    "preventBackNavigation();" +
                    
                    "setTimeout(() => {" +
                        "window.location.replace('" + redirectUrl + "');" +
                    "}, 2000);" +
                    
                    "window.addEventListener('beforeunload', (e) => {" +
                        "if (window.location.pathname.includes('/admin/')) {" +
                            "const msg = 'Durante la impersonación no puedes acceder a páginas de SuperAdmin.';" +
                            "e.returnValue = msg;" +
                            "return msg;" +
                        "}" +
                    "});" +
                "</script>" +
            "</body>" +
            "</html>";
            
        response.getWriter().write(html);
        response.getWriter().flush();
    }
}