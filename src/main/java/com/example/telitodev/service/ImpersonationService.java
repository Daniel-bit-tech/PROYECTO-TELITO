package com.example.telitodev.service;

import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;

/**
 * Servicio para manejar la impersonación de usuarios
 * Permite a un SuperAdmin impersonar a otros usuarios modificando el SecurityContext
 */
@Service
public class ImpersonationService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Inicia la impersonación de un usuario
     * Guarda el contexto original en la sesión y cambia el SecurityContext
     */
    public boolean startImpersonation(String targetUserDni, HttpSession session) {
        try {
            // Verificar que el usuario existe y está activo
            Usuario targetUser = usuarioRepository.findByDni(targetUserDni);
            if (targetUser == null || !targetUser.getEstado()) {
                return false;
            }

            // Verificar que no se trate de impersonar a otro SuperAdmin
            if (isSuperAdmin(targetUser)) {
                return false;
            }

            // Obtener el contexto actual (el admin original)
            Authentication originalAuth = SecurityContextHolder.getContext().getAuthentication();
            
            // Guardar información del admin original en la sesión
            session.setAttribute("IS_IMPERSONATING", true);
            session.setAttribute("ORIGINAL_ADMIN_USERNAME", originalAuth.getName());
            session.setAttribute("ORIGINAL_ADMIN_AUTHORITIES", originalAuth.getAuthorities());
            session.setAttribute("IMPERSONATED_USER_DNI", targetUserDni);
            session.setAttribute("IMPERSONATED_USER_EMAIL", targetUser.getCorreo());
            session.setAttribute("IMPERSONATED_USER_NAME", targetUser.getNombre() + " " + targetUser.getApellidoPaterno());
            session.setAttribute("IMPERSONATED_USER_ROLE", targetUser.getRol().getNombreRol());

            System.out.println("🎭 IMPERSONACIÓN INICIADA:");
            System.out.println("  - Admin original: " + originalAuth.getName());
            System.out.println("  - Usuario objetivo: " + targetUser.getCorreo());
            System.out.println("  - DNI objetivo: " + targetUserDni);
            System.out.println("  - Nombre objetivo: " + targetUser.getNombre() + " " + targetUser.getApellidoPaterno());
            System.out.println("  - Session ID: " + session.getId());
            System.out.println("  - IS_IMPERSONATING set to: " + session.getAttribute("IS_IMPERSONATING"));

            // Crear nueva autenticación con los datos del usuario a impersonar
            List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + targetUser.getRol().getNombreRol())
            );

            // Crear UserDetails para el usuario impersonado
            UserDetails impersonatedUserDetails = User.builder()
                .username(targetUser.getCorreo())
                .password("") // No necesitamos la contraseña para impersonación
                .authorities(authorities)
                .build();

            // Crear nueva autenticación
            Authentication impersonatedAuth = new UsernamePasswordAuthenticationToken(
                impersonatedUserDetails, 
                null, 
                authorities
            );

            // Establecer el nuevo contexto de seguridad
            SecurityContextHolder.getContext().setAuthentication(impersonatedAuth);

            System.out.println("🎭 Impersonación iniciada: " + originalAuth.getName() + " -> " + targetUser.getCorreo());
            System.out.println("✅ Contexto de seguridad actualizado");
            return true;

        } catch (Exception e) {
            System.err.println("Error iniciando impersonación: " + e.getMessage());
            return false;
        }
    }

    /**
     * Detiene la impersonación y restaura el contexto original
     */
    public boolean stopImpersonation(HttpSession session) {
        try {
            Boolean isImpersonating = (Boolean) session.getAttribute("IS_IMPERSONATING");
            if (isImpersonating == null || !isImpersonating) {
                return false;
            }

            // Obtener datos del admin original
            String originalUsername = (String) session.getAttribute("ORIGINAL_ADMIN_USERNAME");
            @SuppressWarnings("unchecked")
            List<GrantedAuthority> originalAuthorities = (List<GrantedAuthority>) session.getAttribute("ORIGINAL_ADMIN_AUTHORITIES");

            if (originalUsername == null) {
                return false;
            }

            // Buscar el usuario admin original
            Usuario originalAdmin = usuarioRepository.findByCorreo(originalUsername);
            if (originalAdmin == null) {
                return false;
            }

            // Crear UserDetails para el admin original
            UserDetails originalUserDetails = User.builder()
                .username(originalUsername)
                .password("") // No necesitamos la contraseña
                .authorities(originalAuthorities != null ? originalAuthorities : 
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPERADMIN")))
                .build();

            // Restaurar autenticación original
            Authentication originalAuth = new UsernamePasswordAuthenticationToken(
                originalUserDetails,
                null,
                originalAuthorities != null ? originalAuthorities : 
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPERADMIN"))
            );

            SecurityContextHolder.getContext().setAuthentication(originalAuth);

            // Limpiar atributos de sesión
            session.removeAttribute("IS_IMPERSONATING");
            session.removeAttribute("ORIGINAL_ADMIN_USERNAME");
            session.removeAttribute("ORIGINAL_ADMIN_AUTHORITIES");
            session.removeAttribute("IMPERSONATED_USER_DNI");
            session.removeAttribute("IMPERSONATED_USER_EMAIL");
            session.removeAttribute("IMPERSONATED_USER_NAME");
            session.removeAttribute("IMPERSONATED_USER_ROLE");

            System.out.println("✅ Impersonación terminada. Volviendo a: " + originalUsername);
            return true;

        } catch (Exception e) {
            System.err.println("Error deteniendo impersonación: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si hay una impersonación activa
     */
    public boolean isImpersonating(HttpSession session) {
        Boolean isImpersonating = (Boolean) session.getAttribute("IS_IMPERSONATING");
        boolean result = isImpersonating != null && isImpersonating;
        
        // Debug: Agregar logs para verificar el estado
        System.out.println("🔍 ImpersonationService.isImpersonating():");
        System.out.println("  - Session ID: " + (session != null ? session.getId() : "null"));
        System.out.println("  - IS_IMPERSONATING attribute: " + isImpersonating);
        System.out.println("  - Result: " + result);
        
        // Debug adicional: mostrar todos los atributos de sesión relacionados con impersonación
        if (session != null) {
            System.out.println("  - ORIGINAL_ADMIN_USERNAME: " + session.getAttribute("ORIGINAL_ADMIN_USERNAME"));
            System.out.println("  - IMPERSONATED_USER_DNI: " + session.getAttribute("IMPERSONATED_USER_DNI"));
            System.out.println("  - IMPERSONATED_USER_NAME: " + session.getAttribute("IMPERSONATED_USER_NAME"));
        }
        
        return result;
    }

    /**
     * Obtiene el DNI del usuario que está siendo impersonado
     */
    public String getImpersonatedUserDni(HttpSession session) {
        return (String) session.getAttribute("IMPERSONATED_USER_DNI");
    }

    /**
     * Obtiene el nombre del usuario que está siendo impersonado
     */
    public String getImpersonatedUserName(HttpSession session) {
        return (String) session.getAttribute("IMPERSONATED_USER_NAME");
    }

    /**
     * Obtiene el rol del usuario que está siendo impersonado
     */
    public String getImpersonatedUserRole(HttpSession session) {
        return (String) session.getAttribute("IMPERSONATED_USER_ROLE");
    }

    /**
     * Obtiene el email del usuario que está siendo impersonado
     */
    public String getImpersonatedUserEmail(HttpSession session) {
        return (String) session.getAttribute("IMPERSONATED_USER_EMAIL");
    }

    /**
     * Verifica si un usuario es SuperAdmin
     */
    private boolean isSuperAdmin(Usuario usuario) {
        return usuario.getRol() != null && "SUPERADMIN".equals(usuario.getRol().getNombreRol());
    }

    /**
     * Obtiene el usuario correcto considerando la impersonación
     * Si hay impersonación activa, devuelve el usuario impersonado
     * Si no hay impersonación, devuelve el usuario autenticado
     */
    public Usuario getCurrentUser(Authentication auth, HttpSession session) {
        Usuario usuario = null;

        if (isImpersonating(session)) {
            String impersonatedDni = getImpersonatedUserDni(session);
            if (impersonatedDni != null) {
                usuario = usuarioRepository.findByDni(impersonatedDni);
                if (usuario != null) {
                    System.out.println("👤 Usuario impersonado obtenido: " + usuario.getDni() + " - " + usuario.getNombre());
                } else {
                    System.err.println("❌ No se encontró usuario impersonado con DNI: " + impersonatedDni);
                }
            }
        }
        
        // Sin impersonación o si falla, usar usuario autenticado normal
        if (usuario == null) {
            usuario = usuarioRepository.findByCorreo(auth.getName());
            if (usuario != null) {
                System.out.println("👤 Usuario autenticado obtenido: " + usuario.getDni() + " - " + usuario.getNombre());
            } else {
                System.err.println("❌ No se encontró usuario autenticado con correo: " + auth.getName());
            }
        }

        // Verificar si el usuario tiene organización
        if (usuario != null && usuario.getOrganizacion() == null) {
            System.err.println("⚠️ ADVERTENCIA: El usuario " + usuario.getDni() + " (" + usuario.getNombre() + ") no tiene organización asignada");
        } else if (usuario != null) {
            System.out.println("🏢 Organización del usuario: " + usuario.getOrganizacion().getNombre() + " (ID: " + usuario.getOrganizacion().getIdOrganizacion() + ")");
        }

        return usuario;
    }
}