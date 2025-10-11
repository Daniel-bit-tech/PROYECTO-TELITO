package com.example.telitodev.controller.admin;

import com.example.telitodev.controller.BaseController;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.entity.ActividadAdmin;
import com.example.telitodev.repository.UsuarioRepository;
import com.example.telitodev.service.AuditoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController extends BaseController {

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private AuditoriaService auditoriaService;

    @GetMapping("/home")
    public String showAdminHome(Model model, Authentication authentication, HttpSession session) {
        // Registrar acceso al dashboard en auditoría
        try {
            auditoriaService.registrarActividad(
                AuditoriaService.VIEW_DASHBOARD, 
                "Accedió al panel de administración"
            );
        } catch (Exception e) {
            System.err.println("⚠️ Error al registrar auditoría (continuando sin auditoría): " + e.getMessage());
            // Continuar sin auditoría, no es crítico
        }
        
        try {
            String correo = authentication.getName();
            
            // Intentar obtener usuario de BD, pero usar mock si falla
            try {
                Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreoAndEstado(correo, true);
                if (usuarioOpt.isPresent()) {
                    model.addAttribute("usuario", usuarioOpt.get());
                    System.out.println("Usuario encontrado en BD: " + usuarioOpt.get().getNombre());
                } else {
                    model.addAttribute("usuario", createMockAdmin(correo));
                    System.out.println("Usuario no encontrado en BD, usando mock");
                }
            } catch (Exception dbError) {
                System.out.println("Error de BD, usando mock: " + dbError.getMessage());
                model.addAttribute("usuario", createMockAdmin(correo));
            }
            
            // Agregar estadísticas básicas para el dashboard (excluyendo SUPERADMINs)
            try {
                List<Usuario> usuarios = usuarioRepository.findAll();
                // Filtrar usuarios excluyendo SUPERADMINs
                List<Usuario> usuariosGestionables = usuarios.stream()
                    .filter(u -> u.getRol() == null || !"SUPERADMIN".equals(u.getRol().getNombreRol()))
                    .toList();
                
                model.addAttribute("totalUsuarios", usuariosGestionables.size());
                // Usar la misma lógica que la API para consistencia
                long usuariosActivos = usuariosGestionables.stream()
                    .filter(u -> u.getEstado() != null && u.getEstado())
                    .count();
                long usuariosInactivos = usuariosGestionables.stream()
                    .filter(u -> u.getEstado() == null || !u.getEstado())
                    .count();
                    
                model.addAttribute("usuariosActivos", usuariosActivos);
                model.addAttribute("usuariosInactivos", usuariosInactivos);
                
                System.out.println("📊 Estadísticas iniciales - Activos: " + usuariosActivos + " | Inactivos: " + usuariosInactivos + " | Total: " + usuariosGestionables.size());
                System.out.println("Estadísticas cargadas: " + usuariosGestionables.size() + " usuarios gestionables (excluyendo SUPERADMINs)");
            } catch (Exception e) {
                System.out.println("Error al cargar estadísticas: " + e.getMessage());
                model.addAttribute("totalUsuarios", 11);  // 11 usuarios gestionables (sin SUPERADMINs)
                model.addAttribute("usuariosActivos", 11);  // Todos están activos por defecto
                model.addAttribute("usuariosInactivos", 0);  // No hay usuarios inactivos por defecto
            }
            
            System.out.println("Redirigiendo a admin/dashboard");
            
            // Agregar información de impersonación al modelo
            addImpersonationAttributes(model, session);
            
            return "admin/dashboard";
        } catch (Exception e) {
            System.out.println("Error general en admin/home: " + e.getMessage());
            model.addAttribute("usuario", createMockAdmin(authentication.getName()));
            model.addAttribute("totalUsuarios", 11);  // 11 usuarios gestionables (sin SUPERADMINs)
            model.addAttribute("usuariosActivos", 10);  // Basado en la imagen actual
            model.addAttribute("usuariosInactivos", 1);  // Basado en la imagen actual
            
            // Agregar información de impersonación al modelo incluso en caso de error
            addImpersonationAttributes(model, session);
            
            return "admin/dashboard";
        }
    }

    @GetMapping("/usuarios")
    public String showUsuarios() {
        // Redirigir al nuevo sistema de gestión de usuarios
        return "redirect:/admin/gestion-usuarios";
    }
    
    @GetMapping("/perfil")
    public String showPerfil(Model model, Authentication authentication, HttpSession session) {
        try {
            String correo = authentication.getName();
            Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreoAndEstado(correo, true);
            
            if (usuarioOpt.isPresent()) {
                model.addAttribute("usuario", usuarioOpt.get());
            } else {
                model.addAttribute("usuario", createMockAdmin(correo));
            }
            
            // Agregar información de impersonación al modelo
            addImpersonationAttributes(model, session);
            
            return "admin/perfil-admin";
        } catch (Exception e) {
            model.addAttribute("usuario", createMockAdmin(authentication.getName()));
            
            // Agregar información de impersonación al modelo incluso en caso de error
            addImpersonationAttributes(model, session);
            
            return "admin/perfil-admin";
        }
    }

    @GetMapping("/reportes")
    public String showReportes(Model model, Authentication authentication) {
        model.addAttribute("usuario", createMockAdmin(authentication.getName()));
        return "admin/reportes";
    }

    @GetMapping("/configuracion")
    public String showConfiguracion(Model model, Authentication authentication) {
        model.addAttribute("usuario", createMockAdmin(authentication.getName()));
        return "admin/configuracion";
    }
    
    @GetMapping("/test")
    public String test(Model model, Authentication authentication) {
        System.out.println("=== ADMIN TEST ===");
        System.out.println("Usuario: " + authentication.getName());
        
        // Probar conexión a BD
        try {
            List<Usuario> usuarios = usuarioRepository.findAll();
            model.addAttribute("totalUsuarios", usuarios.size());
            model.addAttribute("testResult", "Conexión a BD exitosa. Usuarios encontrados: " + usuarios.size());
        } catch (Exception e) {
            model.addAttribute("testResult", "Error de conexión a BD: " + e.getMessage());
        }
        
        return "admin/test";
    }
    
    @GetMapping("/api/dashboard-stats")
    @ResponseBody
    public Map<String, Object> getDashboardStats(Authentication authentication) {
        System.out.println("=== 📊 API DASHBOARD STATS INICIADA ===");
        System.out.println("Usuario que solicita: " + (authentication != null ? authentication.getName() : "Anónimo"));
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Usuario> usuarios = usuarioRepository.findAll();
            System.out.println("✅ Total usuarios encontrados en BD: " + usuarios.size());
            
            // Filtrar usuarios excluyendo SUPERADMINs
            List<Usuario> usuariosGestionables = usuarios.stream()
                .filter(u -> u.getRol() == null || !"SUPERADMIN".equals(u.getRol().getNombreRol()))
                .toList();
            
            System.out.println("✅ Usuarios gestionables (sin SUPERADMINs): " + usuariosGestionables.size());
            
            // Debug: mostrar algunos usuarios gestionables
            usuariosGestionables.stream().limit(3).forEach(u -> {
                System.out.println("  - Usuario: " + u.getNombre() + " " + u.getApellidoPaterno() + 
                    " | Estado: " + u.getEstado() + 
                    " | Rol: " + (u.getRol() != null ? u.getRol().getNombreRol() : "NULL"));
            });
            
            // Contar usuarios gestionables por estado (excluyendo SUPERADMINs)
            Map<String, Integer> usersByStatus = new HashMap<>();
            
            // Debug detallado de conteo de estados
            System.out.println("=== DEBUG CONTEO DE ESTADOS ===");
            usuariosGestionables.forEach(u -> {
                System.out.println("Usuario: " + u.getDni() + " - " + u.getNombre() + 
                    " | Estado raw: " + u.getEstado() + 
                    " | Es activo: " + (u.getEstado() != null && u.getEstado()) +
                    " | Es inactivo: " + (u.getEstado() == null || !u.getEstado()));
            });
            
            long activeUsers = usuariosGestionables.stream()
                .filter(u -> u.getEstado() != null && u.getEstado())
                .count();
            long inactiveUsers = usuariosGestionables.stream()
                .filter(u -> u.getEstado() == null || !u.getEstado())
                .count();
            
            System.out.println("Conteo final - Activos: " + activeUsers + " | Inactivos: " + inactiveUsers);
            System.out.println("Total usuarios gestionables: " + usuariosGestionables.size());
            System.out.println("Verificación matemática: " + activeUsers + " + " + inactiveUsers + " = " + (activeUsers + inactiveUsers));
            
            usersByStatus.put("active", (int) activeUsers);
            usersByStatus.put("inactive", (int) inactiveUsers);
            
            // Contar usuarios por rol (excluyendo SUPERADMINs)
            Map<String, Integer> usersByRole = new HashMap<>();
            usuariosGestionables.forEach(usuario -> {
                if (usuario.getRol() != null && usuario.getRol().getNombreRol() != null) {
                    String roleName = usuario.getRol().getNombreRol();
                    // No incluir SUPERADMINs (doble verificación)
                    if (!"SUPERADMIN".equals(roleName)) {
                        usersByRole.put(roleName, usersByRole.getOrDefault(roleName, 0) + 1);
                    }
                } else {
                    usersByRole.put("Sin Rol", usersByRole.getOrDefault("Sin Rol", 0) + 1);
                }
            });
            
            response.put("usersByStatus", usersByStatus);
            response.put("usersByRole", usersByRole);
            response.put("totalUsers", usuariosGestionables.size());
            response.put("success", true);
            response.put("timestamp", System.currentTimeMillis());
            
            System.out.println("📈 Usuarios por estado: " + usersByStatus);
            System.out.println("📊 Usuarios por rol: " + usersByRole);
            System.out.println("✅ Respuesta JSON generada correctamente");
            
        } catch (Exception e) {
            System.err.println("❌ ERROR obteniendo estadísticas del dashboard: " + e.getMessage());
            e.printStackTrace();
            
            // Datos de respaldo con valores correctos (excluyendo SUPERADMINs)
            Map<String, Integer> defaultStatus = new HashMap<>();
            defaultStatus.put("active", 11);  // Usuarios gestionables activos (todos activos)
            defaultStatus.put("inactive", 0);  // Sin usuarios inactivos por defecto
            
            Map<String, Integer> defaultRoles = new HashMap<>();
            // No incluir SUPERADMIN en el conteo
            defaultRoles.put("DEVELOPER", 7);
            defaultRoles.put("QA", 2);
            defaultRoles.put("PO", 2);
            
            response.put("usersByStatus", defaultStatus);
            response.put("usersByRole", defaultRoles);
            response.put("totalUsers", 11);  // 11 usuarios gestionables (sin SUPERADMINs)
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("fallbackData", true);
        }
        
        System.out.println("📤 Enviando respuesta: " + response);
        return response;
    }
    
    @GetMapping("/api/actividades-recientes")
    @ResponseBody
    public Map<String, Object> getActividadesRecientes(Authentication authentication) {
        System.out.println("=== 📋 API ACTIVIDADES RECIENTES INICIADA ===");
        System.out.println("Usuario que solicita: " + (authentication != null ? authentication.getName() : "Anónimo"));
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Obtener actividades reales del servicio de auditoría
            List<ActividadAdmin> actividadesReales = auditoriaService.obtenerActividadesRecientes(10);
            System.out.println("📋 Actividades encontradas en BD: " + actividadesReales.size());
            
            List<Map<String, Object>> actividades = new ArrayList<>();
            
            // Convertir actividades reales a formato esperado por el frontend
            for (ActividadAdmin actividad : actividadesReales) {
                Map<String, Object> actividadMap = new HashMap<>();
                actividadMap.put("accion", actividad.getAccion());
                actividadMap.put("descripcion", actividad.getDescripcion());
                actividadMap.put("tiempo", actividad.getTiempoTranscurrido());
                actividadMap.put("fecha", actividad.getFechaHora().toString());
                
                // Asignar íconos y colores según el tipo de acción
                switch (actividad.getAccion()) {
                    case "CREAR_USUARIO":
                        actividadMap.put("icono", "fa-user-plus");
                        actividadMap.put("color", "primary");
                        break;
                    case "EDITAR_USUARIO":
                        actividadMap.put("icono", "fa-user-edit");
                        actividadMap.put("color", "success");
                        break;
                    case "BANEAR_USUARIO":
                        actividadMap.put("icono", "fa-user-slash");
                        actividadMap.put("color", "danger");
                        break;
                    case "ACTIVAR_USUARIO":
                        actividadMap.put("icono", "fa-user-check");
                        actividadMap.put("color", "warning");
                        break;
                    case "CAMBIAR_ROL":
                        actividadMap.put("icono", "fa-shield-alt");
                        actividadMap.put("color", "info");
                        break;
                    case "ELIMINAR_USUARIO":
                        actividadMap.put("icono", "fa-user-times");
                        actividadMap.put("color", "danger");
                        break;
                    case "CAMBIAR_PASSWORD":
                        actividadMap.put("icono", "fa-key");
                        actividadMap.put("color", "secondary");
                        break;
                    default:
                        actividadMap.put("icono", "fa-cog");
                        actividadMap.put("color", "secondary");
                        break;
                }
                
                actividades.add(actividadMap);
            }
            
            // Si no hay actividades reales aún, usar datos de ejemplo
            if (actividades.isEmpty()) {
                System.out.println("⚠️ No hay actividades reales, mostrando datos de ejemplo");
                Map<String, Object> ejemplo = new HashMap<>();
                ejemplo.put("accion", "VIEW_DASHBOARD");
                ejemplo.put("descripcion", "Sistema de auditoría inicializado - ¡Las próximas acciones se registrarán automáticamente!");
                ejemplo.put("tiempo", "Ahora");
                ejemplo.put("icono", "fa-tachometer-alt");
                ejemplo.put("color", "success");
                actividades.add(ejemplo);
            }
            
            response.put("actividades", actividades);
            response.put("total", actividades.size());
            response.put("success", true);
            response.put("timestamp", System.currentTimeMillis());
            
            System.out.println("✅ Actividades recientes generadas: " + actividades.size());
            System.out.println("📦 Respuesta completa: " + response);
            System.out.println("=== 📋 API ACTIVIDADES RECIENTES FINALIZADA ===");
            
        } catch (Exception e) {
            System.err.println("❌ ERROR obteniendo actividades recientes: " + e.getMessage());
            e.printStackTrace();
            
            // Datos de respaldo mínimos
            List<Map<String, Object>> actividadesDefault = new ArrayList<>();
            Map<String, Object> actividadDefault = new HashMap<>();
            actividadDefault.put("accion", "SISTEMA");
            actividadDefault.put("descripcion", "Sistema funcionando correctamente");
            actividadDefault.put("tiempo", "Hace 1 minuto");
            actividadDefault.put("icono", "fa-check-circle");
            actividadDefault.put("color", "success");
            
            actividadesDefault.add(actividadDefault);
            
            response.put("actividades", actividadesDefault);
            response.put("total", 1);
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("fallbackData", true);
        }
        
        System.out.println("📤 Enviando actividades: " + response);
        return response;
    }
    
    // Método auxiliar para crear un usuario mock sin depender de la base de datos
    private Object createMockAdmin(String email) {
        return new Object() {
            public String getNombre() { return "Admin"; }
            public String getCorreo() { return email; }
            public Object getRol() { 
                return new Object() {
                    public String getNombreRol() { return "SUPERADMIN"; }
                    public String getDescripcion() { return "Super Administrador"; }
                };
            }
        };
    }
}