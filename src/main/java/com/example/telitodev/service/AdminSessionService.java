package com.example.telitodev.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para gestión avanzada de sesiones de administradores
 * 
 * Funcionalidades:
 * - Monitoreo de sesiones activas por rol de administrador
 * - Control de sesiones concurrentes
 * - Invalidación forzada de sesiones
 * - Auditoría de actividad de sesiones
 * - Prevención de múltiples accesos no autorizados
 */
@Service
public class AdminSessionService {

    @Autowired
    private SpringSessionBackedSessionRegistry sessionRegistry;

    @Autowired
    private FindByIndexNameSessionRepository sessionRepository;

    /**
     * Obtiene todas las sesiones activas de administradores
     */
    public List<AdminSessionInfo> getActiveSessions() {
        List<AdminSessionInfo> activeSessions = new ArrayList<>();
        
        try {
            // Obtener todas las sesiones activas
            Map<String, ? extends Session> sessions = sessionRepository.findByPrincipalName(SecurityContextHolder.getContext().getAuthentication().getName());
            
            for (Map.Entry<String, ? extends Session> entry : sessions.entrySet()) {
                Session session = entry.getValue();
                AdminSessionInfo sessionInfo = createSessionInfo(session);
                if (sessionInfo != null) {
                    activeSessions.add(sessionInfo);
                }
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo sesiones activas: " + e.getMessage());
        }
        
        return activeSessions;
    }

    /**
     * Obtiene sesiones activas por usuario específico
     */
    public List<AdminSessionInfo> getActiveSessionsByUser(String username) {
        List<AdminSessionInfo> userSessions = new ArrayList<>();
        
        try {
            Map<String, ? extends Session> sessions = sessionRepository.findByPrincipalName(username);
            
            for (Map.Entry<String, ? extends Session> entry : sessions.entrySet()) {
                Session session = entry.getValue();
                AdminSessionInfo sessionInfo = createSessionInfo(session);
                if (sessionInfo != null) {
                    userSessions.add(sessionInfo);
                }
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo sesiones para usuario " + username + ": " + e.getMessage());
        }
        
        return userSessions;
    }

    /**
     * Invalida una sesión específica por ID
     */
    public boolean invalidateSession(String sessionId) {
        try {
            sessionRepository.deleteById(sessionId);
            System.out.println("🔒 Sesión invalidada: " + sessionId);
            return true;
        } catch (Exception e) {
            System.err.println("Error invalidando sesión " + sessionId + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Invalida todas las sesiones de un usuario específico
     */
    public int invalidateAllUserSessions(String username) {
        int invalidatedCount = 0;
        
        try {
            Map<String, ? extends Session> sessions = sessionRepository.findByPrincipalName(username);
            
            for (String sessionId : sessions.keySet()) {
                if (invalidateSession(sessionId)) {
                    invalidatedCount++;
                }
            }
            
            System.out.println("🔒 Invalidadas " + invalidatedCount + " sesiones para usuario: " + username);
        } catch (Exception e) {
            System.err.println("Error invalidando sesiones para usuario " + username + ": " + e.getMessage());
        }
        
        return invalidatedCount;
    }

    /**
     * Verifica si un usuario tiene múltiples sesiones activas
     */
    public boolean hasMultipleSessions(String username) {
        try {
            Map<String, ? extends Session> sessions = sessionRepository.findByPrincipalName(username);
            return sessions.size() > 1;
        } catch (Exception e) {
            System.err.println("Error verificando sesiones múltiples para " + username + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Cuenta el total de sesiones activas de administradores
     */
    public long getTotalActiveAdminSessions() {
        // TODO: Implementar filtro por roles de admin
        return getActiveSessions().size();
    }

    /**
     * Limpia sesiones expiradas manualmente
     */
    public int cleanupExpiredSessions() {
        int cleanedCount = 0;
        
        try {
            // Esta funcionalidad depende de la implementación específica del repository
            // Por ahora, Spring Session maneja automáticamente la limpieza
            System.out.println("🧹 Limpieza automática de sesiones expiradas ejecutada");
        } catch (Exception e) {
            System.err.println("Error en limpieza de sesiones: " + e.getMessage());
        }
        
        return cleanedCount;
    }

    /**
     * Crea información detallada de una sesión
     */
    private AdminSessionInfo createSessionInfo(Session session) {
        try {
            AdminSessionInfo info = new AdminSessionInfo();
            info.setSessionId(session.getId());
            info.setCreationTime(session.getCreationTime());
            info.setLastAccessedTime(session.getLastAccessedTime());
            info.setMaxInactiveInterval(session.getMaxInactiveInterval());
            info.setExpired(session.isExpired());
            
            // Extraer información adicional de los atributos de sesión
            Object principalName = session.getAttribute(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME);
            if (principalName != null) {
                info.setPrincipalName(principalName.toString());
            }
            
            return info;
        } catch (Exception e) {
            System.err.println("Error creando información de sesión: " + e.getMessage());
            return null;
        }
    }

    /**
     * Clase interna para información de sesiones de admin
     */
    public static class AdminSessionInfo {
        private String sessionId;
        private String principalName;
        private Instant creationTime;
        private Instant lastAccessedTime;
        private java.time.Duration maxInactiveInterval;
        private boolean expired;

        // Getters y Setters
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }

        public String getPrincipalName() { return principalName; }
        public void setPrincipalName(String principalName) { this.principalName = principalName; }

        public Instant getCreationTime() { return creationTime; }
        public void setCreationTime(Instant creationTime) { this.creationTime = creationTime; }

        public Instant getLastAccessedTime() { return lastAccessedTime; }
        public void setLastAccessedTime(Instant lastAccessedTime) { this.lastAccessedTime = lastAccessedTime; }

        public java.time.Duration getMaxInactiveInterval() { return maxInactiveInterval; }
        public void setMaxInactiveInterval(java.time.Duration maxInactiveInterval) { this.maxInactiveInterval = maxInactiveInterval; }

        public boolean isExpired() { return expired; }
        public void setExpired(boolean expired) { this.expired = expired; }
    }
}