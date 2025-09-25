package com.example.telitodev.filter;

import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que verifica en tiempo real si el usuario autenticado sigue activo
 * Si el usuario ha sido desactivado, lo desloguea automáticamente
 */
@Component
public class UsuarioActivoFilter extends OncePerRequestFilter {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        // Obtener la autenticación actual
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Solo verificar si hay un usuario autenticado
        if (auth != null && auth.isAuthenticated() && 
            !auth.getName().equals("anonymousUser")) {
            
            String correoUsuario = auth.getName();
            String requestURI = request.getRequestURI();
            
            // Excluir ciertas rutas del filtro (login, logout, recursos estáticos, etc.)
            if (shouldSkipFilter(requestURI)) {
                filterChain.doFilter(request, response);
                return;
            }
            
            try {
                // Verificar el estado actual del usuario en la base de datos
                Usuario usuario = usuarioRepository.findByCorreo(correoUsuario);
                
                if (usuario == null || !usuario.getEstado()) {
                    // Usuario no existe o está desactivado - forzar logout
                    System.out.println("🚫 USUARIO DESACTIVADO DETECTADO EN TIEMPO REAL:");
                    System.out.println("   - Email: " + correoUsuario);
                    System.out.println("   - URI: " + requestURI);
                    System.out.println("   - Usuario existe: " + (usuario != null));
                    if (usuario != null) {
                        System.out.println("   - Estado: " + usuario.getEstado());
                    }
                    System.out.println("   - Acción: Forzando logout y redirección");
                    
                    // Limpiar el contexto de seguridad
                    new SecurityContextLogoutHandler().logout(request, response, auth);
                    
                    // Verificar si es una petición AJAX
                    if (isAjaxRequest(request)) {
                        // Respuesta JSON para peticiones AJAX
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\": \"Usuario desactivado\", \"redirect\": \"/login?error=disabled\"}");
                    } else {
                        // Redirección normal para peticiones regulares
                        response.sendRedirect("/login?error=disabled");
                    }
                    return;
                }
                
            } catch (Exception e) {
                System.err.println("❌ Error verificando estado del usuario: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Continuar con la cadena de filtros si todo está OK
        filterChain.doFilter(request, response);
    }
    
    /**
     * Determina si se debe omitir el filtro para ciertas rutas
     */
    private boolean shouldSkipFilter(String requestURI) {
        return requestURI.startsWith("/login") ||
               requestURI.startsWith("/logout") ||
               requestURI.startsWith("/css") ||
               requestURI.startsWith("/js") ||
               requestURI.startsWith("/img") ||
               requestURI.startsWith("/webjars") ||
               requestURI.startsWith("/static") ||
               requestURI.startsWith("/error") ||
               requestURI.startsWith("/favicon.ico") ||
               requestURI.equals("/");
    }
    
    /**
     * Verifica si la petición es AJAX
     */
    private boolean isAjaxRequest(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equals(requestedWith) ||
               request.getContentType() != null && request.getContentType().contains("application/json");
    }
}