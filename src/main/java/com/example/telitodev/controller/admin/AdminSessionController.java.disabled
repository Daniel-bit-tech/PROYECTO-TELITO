package com.example.telitodev.controller.admin;

import com.example.telitodev.service.AdminSessionService;
import com.example.telitodev.service.AuditoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controlador para el monitoreo y gestión de sesiones de administradores
 * 
 * Funcionalidades:
 * - Vista de sesiones activas
 * - Invalidación de sesiones específicas
 * - Invalidación masiva por usuario
 * - Estadísticas de sesiones
 * - Auditoría de acciones de gestión de sesiones
 */
@Controller
@RequestMapping("/admin/sesiones")
@PreAuthorize("hasRole('SUPERADMIN')")
public class AdminSessionController {

    @Autowired
    private AdminSessionService adminSessionService;

    @Autowired
    private AuditoriaService auditoriaService;

    /**
     * Dashboard principal de monitoreo de sesiones
     */
    @GetMapping
    public String sessionDashboard(Model model, Authentication auth) {
        try {
            // Obtener todas las sesiones activas
            List<AdminSessionService.AdminSessionInfo> activeSessions = adminSessionService.getActiveSessions();
            
            // Estadísticas
            long totalSessions = adminSessionService.getTotalActiveAdminSessions();
            
            model.addAttribute("activeSessions", activeSessions);
            model.addAttribute("totalSessions", totalSessions);
            model.addAttribute("currentUser", auth.getName());
            
            // Auditoría
            auditoriaService.registrarActividad(
                auth.getName(),
                "CONSULTA_SESIONES",
                "Acceso al dashboard de sesiones administrativas"
            );
            
            return "admin/sesiones/dashboard";
            
        } catch (Exception e) {
            System.err.println("Error en dashboard de sesiones: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar información de sesiones");
            return "admin/sesiones/dashboard";
        }
    }

    /**
     * Vista de sesiones por usuario específico
     */
    @GetMapping("/usuario/{username}")
    public String userSessions(@PathVariable String username, Model model, Authentication auth) {
        try {
            List<AdminSessionService.AdminSessionInfo> userSessions = adminSessionService.getActiveSessionsByUser(username);
            boolean hasMultipleSessions = adminSessionService.hasMultipleSessions(username);
            
            model.addAttribute("userSessions", userSessions);
            model.addAttribute("username", username);
            model.addAttribute("hasMultipleSessions", hasMultipleSessions);
            model.addAttribute("currentUser", auth.getName());
            
            // Auditoría
            auditoriaService.registrarActividad(
                auth.getName(),
                "CONSULTA_SESIONES_USUARIO",
                "Consulta de sesiones para usuario: " + username
            );
            
            return "admin/sesiones/user-sessions";
            
        } catch (Exception e) {
            System.err.println("Error consultando sesiones de usuario " + username + ": " + e.getMessage());
            model.addAttribute("error", "Error al consultar sesiones del usuario");
            return "admin/sesiones/dashboard";
        }
    }

    /**
     * Invalidar una sesión específica
     */
    @PostMapping("/invalidar/{sessionId}")
    public String invalidateSession(@PathVariable String sessionId, 
                                  RedirectAttributes redirectAttributes, 
                                  Authentication auth) {
        try {
            boolean success = adminSessionService.invalidateSession(sessionId);
            
            if (success) {
                redirectAttributes.addFlashAttribute("success", "Sesión invalidada exitosamente");
                
                // Auditoría
                auditoriaService.registrarActividad(
                    auth.getName(),
                    "INVALIDAR_SESION",
                    "Sesión invalidada: " + sessionId
                );
            } else {
                redirectAttributes.addFlashAttribute("error", "No se pudo invalidar la sesión");
            }
            
        } catch (Exception e) {
            System.err.println("Error invalidando sesión " + sessionId + ": " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al invalidar la sesión");
        }
        
        return "redirect:/admin/sesiones";
    }

    /**
     * Invalidar todas las sesiones de un usuario
     */
    @PostMapping("/invalidar-usuario/{username}")
    public String invalidateAllUserSessions(@PathVariable String username, 
                                          RedirectAttributes redirectAttributes, 
                                          Authentication auth) {
        try {
            // Prevenir que el admin se invalide sus propias sesiones
            if (username.equals(auth.getName())) {
                redirectAttributes.addFlashAttribute("warning", "No puedes invalidar tus propias sesiones");
                return "redirect:/admin/sesiones";
            }
            
            int invalidatedCount = adminSessionService.invalidateAllUserSessions(username);
            
            if (invalidatedCount > 0) {
                redirectAttributes.addFlashAttribute("success", 
                    "Se invalidaron " + invalidatedCount + " sesiones del usuario " + username);
                
                // Auditoría
                auditoriaService.registrarActividad(
                    auth.getName(),
                    "INVALIDAR_SESIONES_USUARIO",
                    "Invalidadas " + invalidatedCount + " sesiones del usuario: " + username
                );
            } else {
                redirectAttributes.addFlashAttribute("info", "No hay sesiones activas para invalidar");
            }
            
        } catch (Exception e) {
            System.err.println("Error invalidando sesiones de usuario " + username + ": " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al invalidar sesiones del usuario");
        }
        
        return "redirect:/admin/sesiones";
    }

    /**
     * API endpoint para obtener estadísticas de sesiones en tiempo real
     */
    @GetMapping("/api/estadisticas")
    @ResponseBody
    public SessionStats getSessionStats(Authentication auth) {
        try {
            long totalSessions = adminSessionService.getTotalActiveAdminSessions();
            List<AdminSessionService.AdminSessionInfo> activeSessions = adminSessionService.getActiveSessions();
            
            // Auditoría
            auditoriaService.registrarActividad(
                auth.getName(),
                "API_ESTADISTICAS_SESIONES",
                "Consulta API de estadísticas de sesiones"
            );
            
            return new SessionStats(totalSessions, activeSessions.size());
            
        } catch (Exception e) {
            System.err.println("Error obteniendo estadísticas de sesiones: " + e.getMessage());
            return new SessionStats(0, 0);
        }
    }

    /**
     * Limpieza manual de sesiones expiradas
     */
    @PostMapping("/limpiar-expiradas")
    public String cleanupExpiredSessions(RedirectAttributes redirectAttributes, Authentication auth) {
        try {
            int cleanedCount = adminSessionService.cleanupExpiredSessions();
            
            redirectAttributes.addFlashAttribute("success", 
                "Limpieza de sesiones completada. Sesiones procesadas: " + cleanedCount);
            
            // Auditoría
            auditoriaService.registrarActividad(
                auth.getName(),
                "LIMPIEZA_SESIONES",
                "Limpieza manual de sesiones expiradas"
            );
            
        } catch (Exception e) {
            System.err.println("Error en limpieza de sesiones: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error durante la limpieza de sesiones");
        }
        
        return "redirect:/admin/sesiones";
    }

    /**
     * Clase para estadísticas de sesiones
     */
    public static class SessionStats {
        private long totalSessions;
        private int activeSessions;

        public SessionStats(long totalSessions, int activeSessions) {
            this.totalSessions = totalSessions;
            this.activeSessions = activeSessions;
        }

        // Getters
        public long getTotalSessions() { return totalSessions; }
        public int getActiveSessions() { return activeSessions; }
    }
}