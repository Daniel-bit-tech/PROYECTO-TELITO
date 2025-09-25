package com.example.telitodev.service;

import com.example.telitodev.entity.ActividadAdmin;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.ActividadAdminRepository;
import com.example.telitodev.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio para registrar y consultar actividades administrativas
 * Proporciona auditoría completa de todas las acciones de administradores
 */
@Service
public class AuditoriaService {
    
    @Autowired
    private ActividadAdminRepository actividadAdminRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Constantes para tipos de acciones
    public static final String CREAR_USUARIO = "CREAR_USUARIO";
    public static final String EDITAR_USUARIO = "EDITAR_USUARIO";
    public static final String BANEAR_USUARIO = "BANEAR_USUARIO";
    public static final String ACTIVAR_USUARIO = "ACTIVAR_USUARIO";
    public static final String CAMBIAR_ROL = "CAMBIAR_ROL";
    public static final String ELIMINAR_USUARIO = "ELIMINAR_USUARIO";
    public static final String LOGIN_ADMIN = "LOGIN_ADMIN";
    public static final String EXPORTAR_DATOS = "EXPORTAR_DATOS";
    public static final String CAMBIAR_PASSWORD = "CAMBIAR_PASSWORD";
    public static final String VIEW_DASHBOARD = "VIEW_DASHBOARD";
    
    /**
     * Registra una nueva actividad administrativa
     */
    public void registrarActividad(String accion, String descripcion) {
        registrarActividad(accion, descripcion, null, null, null);
    }
    
    /**
     * Registra una actividad que afecta a un usuario específico
     */
    public void registrarActividad(String accion, String descripcion, String usuarioAfectadoDni) {
        registrarActividad(accion, descripcion, usuarioAfectadoDni, null, null);
    }
    
    /**
     * Registra una actividad con información del request HTTP
     */
    public void registrarActividad(String accion, String descripcion, String usuarioAfectadoDni, 
                                 HttpServletRequest request) {
        String ipAddress = request != null ? getClientIpAddress(request) : null;
        String userAgent = request != null ? request.getHeader("User-Agent") : null;
        
        registrarActividad(accion, descripcion, usuarioAfectadoDni, ipAddress, userAgent);
    }
    
    /**
     * Registra una actividad completa con todos los detalles
     */
    public void registrarActividad(String accion, String descripcion, String usuarioAfectadoDni, 
                                 String ipAddress, String userAgent) {
        try {
            // Obtener el admin actual del contexto de seguridad
            String adminDni = obtenerAdminActual();
            if (adminDni == null) {
                System.err.println("⚠️ No se pudo identificar el admin actual para la auditoría");
                return;
            }
            
            // Crear la actividad
            ActividadAdmin actividad = new ActividadAdmin();
            actividad.setAccion(accion);
            actividad.setDescripcion(descripcion);
            actividad.setUsuarioAdminDni(adminDni);
            actividad.setUsuarioAfectadoDni(usuarioAfectadoDni);
            actividad.setFechaHora(LocalDateTime.now());
            actividad.setIpAddress(ipAddress);
            actividad.setUserAgent(userAgent);
            
            // Crear detalles adicionales en JSON
            Map<String, Object> detalles = new HashMap<>();
            detalles.put("timestamp", System.currentTimeMillis());
            detalles.put("admin_dni", adminDni);
            if (usuarioAfectadoDni != null) {
                detalles.put("usuario_afectado_dni", usuarioAfectadoDni);
            }
            
            actividad.setDetalles(objectMapper.writeValueAsString(detalles));
            
            // Guardar en la base de datos
            actividadAdminRepository.save(actividad);
            
            System.out.println("✅ Actividad registrada: " + accion + " - " + descripcion);
            
        } catch (Exception e) {
            System.err.println("❌ Error registrando actividad de auditoría: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Registra actividad con detalles personalizados en JSON
     */
    public void registrarActividadConDetalles(String accion, String descripcion, 
                                            String usuarioAfectadoDni, Map<String, Object> detallesCustom) {
        try {
            String adminDni = obtenerAdminActual();
            if (adminDni == null) return;
            
            ActividadAdmin actividad = new ActividadAdmin();
            actividad.setAccion(accion);
            actividad.setDescripcion(descripcion);
            actividad.setUsuarioAdminDni(adminDni);
            actividad.setUsuarioAfectadoDni(usuarioAfectadoDni);
            actividad.setFechaHora(LocalDateTime.now());
            
            // Agregar detalles personalizados
            if (detallesCustom != null) {
                detallesCustom.put("timestamp", System.currentTimeMillis());
                detallesCustom.put("admin_dni", adminDni);
                actividad.setDetalles(objectMapper.writeValueAsString(detallesCustom));
            }
            
            actividadAdminRepository.save(actividad);
            
        } catch (Exception e) {
            System.err.println("❌ Error registrando actividad con detalles: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene las últimas actividades para el dashboard
     */
    public List<ActividadAdmin> obtenerActividadesRecientes(int limit) {
        return actividadAdminRepository.findTopActivities(limit);
    }
    
    /**
     * Obtiene actividades paginadas
     */
    public Page<ActividadAdmin> obtenerActividades(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return actividadAdminRepository.findLatestActivities(pageable);
    }
    
    /**
     * Obtiene actividades de un admin específico
     */
    public Page<ActividadAdmin> obtenerActividadesPorAdmin(String adminDni, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return actividadAdminRepository.findByUsuarioAdminDniOrderByFechaHoraDesc(adminDni, pageable);
    }
    
    /**
     * Obtiene actividades por tipo de acción
     */
    public Page<ActividadAdmin> obtenerActividadesPorAccion(String accion, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return actividadAdminRepository.findByAccionOrderByFechaHoraDesc(accion, pageable);
    }
    
    /**
     * Obtiene estadísticas de actividades
     */
    public Map<String, Long> obtenerEstadisticasActividades() {
        Map<String, Long> estadisticas = new HashMap<>();
        
        estadisticas.put("total", actividadAdminRepository.count());
        estadisticas.put("crear_usuario", actividadAdminRepository.countByAccion(CREAR_USUARIO));
        estadisticas.put("editar_usuario", actividadAdminRepository.countByAccion(EDITAR_USUARIO));
        estadisticas.put("banear_usuario", actividadAdminRepository.countByAccion(BANEAR_USUARIO));
        estadisticas.put("activar_usuario", actividadAdminRepository.countByAccion(ACTIVAR_USUARIO));
        
        return estadisticas;
    }
    
    /**
     * Obtiene las actividades de hoy para el dashboard
     */
    public List<ActividadAdmin> obtenerActividadesHoy() {
        return actividadAdminRepository.findTodayActivitiesForDashboard();
    }
    
    // === MÉTODOS AUXILIARES ===
    
    /**
     * Obtiene el DNI del administrador actual desde el contexto de seguridad
     */
    private String obtenerAdminActual() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }
            
            String correo = authentication.getName();
            Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreoAndEstado(correo, true);
            
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                // Verificar que sea admin
                if (usuario.getRol() != null && "SUPERADMIN".equals(usuario.getRol().getNombreRol())) {
                    return usuario.getDni();
                }
            }
            
            return null;
        } catch (Exception e) {
            System.err.println("Error obteniendo admin actual: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Extrae la IP real del cliente considerando proxies y load balancers
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
            "X-Forwarded-For",
            "X-Real-IP", 
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };
        
        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // Si hay múltiples IPs, tomar la primera
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }
        
        return request.getRemoteAddr();
    }
}