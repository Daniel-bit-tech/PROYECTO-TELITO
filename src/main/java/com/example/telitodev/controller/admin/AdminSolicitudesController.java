//package com.example.telitodev.controller.admin;
//
//import com.example.telitodev.controller.BaseController;
//import com.example.telitodev.dto.SolicitudAccesoDecisionRequest;
//import com.example.telitodev.dto.SolicitudAccesoResponse;
//import com.example.telitodev.entity.*;
//import com.example.telitodev.repository.UsuarioRepository;
//import com.example.telitodev.repository.SolicitudAccesoRepository;
//import com.example.telitodev.service.OnboardingService;
//import com.example.telitodev.service.OrganizacionService;
//import com.example.telitodev.service.SolAccesoOrgService;
//import com.example.telitodev.service.AuditoriaService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.Authentication;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import jakarta.servlet.http.HttpSession;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Controller
//@RequestMapping("/admin")
//@PreAuthorize("hasRole('SUPERADMIN')")
//public class AdminSolicitudesController extends BaseController {
//
//    @Autowired
//    private OnboardingService onboardingService;
//
//    @Autowired
//    private SolAccesoOrgService solAccesoOrgService;
//
//    @Autowired
//    private OrganizacionService organizacionService;
//
//    @Autowired
//    private UsuarioRepository usuarioRepository;
//
//    @Autowired
//    private SolicitudAccesoRepository solicitudAccesoRepository;
//
//    @Autowired
//    private AuditoriaService auditoriaService;
//
//    /**
//     * Endpoint de prueba para verificar que el controlador funciona
//     */
//    @PostMapping("/solicitudes/test")
//    @ResponseBody
//    public ResponseEntity<String> testEndpoint() {
//        return ResponseEntity.ok("Controlador AdminSolicitudes funcionando correctamente");
//    }
//
//    /**
//     * Vista principal de gestión de solicitudes para SuperAdmin
//     */
//    @GetMapping("/solicitudes")
//    public String mostrarSolicitudes(Model model, Authentication authentication, HttpSession session) {
//        try {
//            // Registrar acceso en auditoría
//            auditoriaService.registrarActividad(
//                "VIEW_SOLICITUDES",
//                "SuperAdmin accedió a la gestión de solicitudes"
//            );
//
//            // Obtener usuario actual
//            Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());
//            model.addAttribute("usuario", usuario);
//
//            // Agregar información de impersonación
//            addImpersonationAttributes(model, session);
//
//            // === SOLICITUDES DE ACCESO A ORGANIZACIONES (SolAccesoOrgService) ===
//            List<SolAccesoOrg> solicitudesOrganizacion = solAccesoOrgService.obtenerSolicitudesPendientes();
//            model.addAttribute("solicitudesOrganizacion", solicitudesOrganizacion);
//
//            // === ESTADÍSTICAS PARA ORGANIZACIONES ===
//
//            // Solicitudes Organización
//            long totalOrgAprobadas = solAccesoOrgService.contarSolicitudesPorEstado(SolAccesoOrg.EstadoSolicitud.APROBADA);
//            long totalOrgRechazadas = solAccesoOrgService.contarSolicitudesPorEstado(SolAccesoOrg.EstadoSolicitud.RECHAZADA);
//            long totalOrgPendientes = solicitudesOrganizacion.size();
//
//            model.addAttribute("totalOrgAprobadas", totalOrgAprobadas);
//            model.addAttribute("totalOrgRechazadas", totalOrgRechazadas);
//            model.addAttribute("totalOrgPendientes", totalOrgPendientes);
//
//            // === LISTADO DE ORGANIZACIONES (para filtros) ===
//            List<Organizacion> organizaciones = organizacionService.obtenerTodasOrganizacionesOrdenadas();
//            model.addAttribute("organizaciones", organizaciones);
//
//            System.out.println("=== SOLICITUDES CARGADAS ===");
//            System.out.println("Organización Pendientes: " + totalOrgPendientes);
//
//            return "admin/solicitudes";
//
//        } catch (Exception e) {
//            System.err.println("Error en AdminSolicitudesController: " + e.getMessage());
//            e.printStackTrace();
//            model.addAttribute("error", "Error al cargar las solicitudes: " + e.getMessage());
//            return "admin/solicitudes";
//        }
//    }
//
//    /**
//     * Aprobar solicitud de acceso a API
//     */
//    @PostMapping("/solicitudes/api/{id}/aprobar")
//    public String aprobarSolicitudAPI(
//            @PathVariable Integer id,
//            @RequestParam(required = false) String comentarios,
//            Authentication authentication,
//            RedirectAttributes redirectAttributes) {
//
//        try {
//            System.out.println("=== APROBAR SOLICITUD API ===");
//            System.out.println("ID Solicitud: " + id);
//            System.out.println("Comentarios: " + comentarios);
//            System.out.println("Usuario: " + authentication.getName());
//
//            Usuario admin = usuarioRepository.findByCorreo(authentication.getName());
//            if (admin == null) {
//                throw new RuntimeException("Usuario administrador no encontrado");
//            }
//
//            // Verificar que la solicitud existe
//            if (!solicitudAccesoRepository.existsById(id)) {
//                throw new RuntimeException("Solicitud con ID " + id + " no encontrada");
//            }
//
//            // Crear request de decisión
//            SolicitudAccesoDecisionRequest decision = new SolicitudAccesoDecisionRequest();
//            decision.setAccion("APROBAR");
//            decision.setMotivoRechazo(comentarios != null ? comentarios : "Aprobada por SuperAdmin");
//
//            System.out.println("Decision creada: " + decision.getAccion());
//
//            // Procesar la solicitud
//            onboardingService.procesarSolicitud(id, decision);
//
//            System.out.println("Solicitud procesada exitosamente");
//
//            // Registrar en auditoría
//            auditoriaService.registrarActividad(
//                "APROBAR_SOLICITUD_API",
//                "SuperAdmin aprobó solicitud API #" + id + " - " + decision.getMotivoRechazo()
//            );
//
//            redirectAttributes.addFlashAttribute("success",
//                "✅ Solicitud de API #" + id + " aprobada exitosamente");
//
//        } catch (Exception e) {
//            System.err.println("Error al aprobar solicitud: " + e.getMessage());
//            e.printStackTrace();
//            redirectAttributes.addFlashAttribute("error",
//                "❌ Error al aprobar solicitud: " + e.getMessage());
//        }
//
//        return "redirect:/admin/solicitudes";
//    }
//
//    /**
//     * Rechazar solicitud de acceso a API
//     */
//    @PostMapping("/solicitudes/api/{id}/rechazar")
//    public String rechazarSolicitudAPI(
//            @PathVariable Integer id,
//            @RequestParam(required = false) String comentarios,
//            Authentication authentication,
//            RedirectAttributes redirectAttributes) {
//
//        try {
//            System.out.println("=== RECHAZAR SOLICITUD API ===");
//            System.out.println("ID Solicitud: " + id);
//            System.out.println("Comentarios: " + comentarios);
//            System.out.println("Usuario: " + authentication.getName());
//
//            Usuario admin = usuarioRepository.findByCorreo(authentication.getName());
//            if (admin == null) {
//                throw new RuntimeException("Usuario administrador no encontrado");
//            }
//
//            // Verificar que la solicitud existe
//            if (!solicitudAccesoRepository.existsById(id)) {
//                throw new RuntimeException("Solicitud con ID " + id + " no encontrada");
//            }
//
//            // Crear request de decisión
//            SolicitudAccesoDecisionRequest decision = new SolicitudAccesoDecisionRequest();
//            decision.setAccion("RECHAZAR");
//            decision.setMotivoRechazo(comentarios != null ? comentarios : "Rechazada por SuperAdmin");
//
//            System.out.println("Decision creada: " + decision.getAccion());
//
//            // Procesar la solicitud
//            onboardingService.procesarSolicitud(id, decision);
//
//            System.out.println("Solicitud procesada exitosamente");
//
//            // Registrar en auditoría
//            auditoriaService.registrarActividad(
//                "RECHAZAR_SOLICITUD_API",
//                "SuperAdmin rechazó solicitud API #" + id + " - " + decision.getMotivoRechazo()
//            );
//
//            redirectAttributes.addFlashAttribute("warning",
//                "❌ Solicitud de API #" + id + " rechazada");
//
//        } catch (Exception e) {
//            System.err.println("Error al rechazar solicitud: " + e.getMessage());
//            e.printStackTrace();
//            redirectAttributes.addFlashAttribute("error",
//                "❌ Error al rechazar solicitud: " + e.getMessage());
//        }
//
//        return "redirect:/admin/solicitudes";
//    }
//
//    /**
//     * Aprobar solicitud de acceso a organización
//     */
//    @PostMapping("/solicitudes/organizacion/{id}/aprobar")
//    public String aprobarSolicitudOrganizacion(
//            @PathVariable Integer id,
//            @RequestParam(required = false) String comentarios,
//            Authentication authentication,
//            RedirectAttributes redirectAttributes) {
//
//        System.out.println("=== ENDPOINT ALCANZADO: APROBAR SOLICITUD ORGANIZACIÓN ===");
//        System.out.println("ID Solicitud: " + id);
//        System.out.println("Comentarios: " + comentarios);
//        System.out.println("Usuario: " + (authentication != null ? authentication.getName() : "null"));
//
//        try {
//            // Verificar autenticación
//            if (authentication == null) {
//                throw new RuntimeException("Usuario no autenticado");
//            }
//
//            Usuario admin = usuarioRepository.findByCorreo(authentication.getName());
//            if (admin == null) {
//                throw new RuntimeException("Usuario administrador no encontrado: " + authentication.getName());
//            }
//
//            System.out.println("Usuario encontrado: " + admin.getNombre() + " - DNI: " + admin.getDni());
//
//            // Verificar que el servicio esté disponible
//            if (solAccesoOrgService == null) {
//                throw new RuntimeException("Servicio SolAccesoOrgService no disponible");
//            }
//
//            // Aprobar usando el servicio existente
//            SolAccesoOrg solicitud = solAccesoOrgService.aprobarSolicitud(id, admin.getDni(), comentarios);
//
//            System.out.println("Solicitud de organización procesada exitosamente para DNI: " + solicitud.getDni());
//
//            // Registrar en auditoría si el servicio está disponible
//            if (auditoriaService != null) {
//                auditoriaService.registrarActividad(
//                    "APROBAR_SOLICITUD_ORG",
//                    "SuperAdmin aprobó solicitud de organización #" + id + " para " + solicitud.getDni()
//                );
//            }
//
//            redirectAttributes.addFlashAttribute("success",
//                "✅ Solicitud de organización #" + id + " aprobada exitosamente");
//
//        } catch (Exception e) {
//            System.err.println("Error al aprobar solicitud de organización: " + e.getMessage());
//            e.printStackTrace();
//            redirectAttributes.addFlashAttribute("error",
//                "❌ Error al aprobar solicitud: " + e.getMessage());
//        }
//
//        return "redirect:/admin/solicitudes";
//    }
//
//    /**
//     * Rechazar solicitud de acceso a organización
//     */
//    @PostMapping("/solicitudes/organizacion/{id}/rechazar")
//    public String rechazarSolicitudOrganizacion(
//            @PathVariable Integer id,
//            @RequestParam(required = false) String comentarios,
//            Authentication authentication,
//            RedirectAttributes redirectAttributes) {
//
//        System.out.println("=== ENDPOINT ALCANZADO: RECHAZAR SOLICITUD ORGANIZACIÓN ===");
//        System.out.println("ID Solicitud: " + id);
//        System.out.println("Comentarios: " + comentarios);
//        System.out.println("Usuario: " + (authentication != null ? authentication.getName() : "null"));
//
//        try {
//            // Verificar autenticación
//            if (authentication == null) {
//                throw new RuntimeException("Usuario no autenticado");
//            }
//
//            Usuario admin = usuarioRepository.findByCorreo(authentication.getName());
//            if (admin == null) {
//                throw new RuntimeException("Usuario administrador no encontrado: " + authentication.getName());
//            }
//
//            System.out.println("Usuario encontrado: " + admin.getNombre() + " - DNI: " + admin.getDni());
//
//            // Verificar que el servicio esté disponible
//            if (solAccesoOrgService == null) {
//                throw new RuntimeException("Servicio SolAccesoOrgService no disponible");
//            }
//
//            // Rechazar usando el servicio existente
//            SolAccesoOrg solicitud = solAccesoOrgService.rechazarSolicitud(id, admin.getDni(), comentarios);
//
//            System.out.println("Solicitud de organización procesada exitosamente para DNI: " + solicitud.getDni());
//
//            // Registrar en auditoría si el servicio está disponible
//            if (auditoriaService != null) {
//                auditoriaService.registrarActividad(
//                    "RECHAZAR_SOLICITUD_ORG",
//                    "SuperAdmin rechazó solicitud de organización #" + id + " para " + solicitud.getDni()
//                );
//            }
//
//            redirectAttributes.addFlashAttribute("warning",
//                "❌ Solicitud de organización #" + id + " rechazada");
//
//        } catch (Exception e) {
//            System.err.println("Error al rechazar solicitud de organización: " + e.getMessage());
//            e.printStackTrace();
//            redirectAttributes.addFlashAttribute("error",
//                "❌ Error al rechazar solicitud: " + e.getMessage());
//        }
//
//        return "redirect:/admin/solicitudes";
//    }
//
//    /**
//     * API para obtener estadísticas de solicitudes (para gráficos)
//     */
//    @GetMapping("/api/solicitudes/estadisticas")
//    @ResponseBody
//    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
//        try {
//            Map<String, Object> estadisticas = new HashMap<>();
//
//            // Estadísticas de solicitudes API
//            Map<String, Object> statsAPI = new HashMap<>();
//            statsAPI.put("pendientes", solicitudAccesoRepository.findByEstado(false).size());
//            statsAPI.put("aprobadas", solicitudAccesoRepository.findByEstado(true).size());
//            statsAPI.put("rechazadas", 0); // Necesitaríamos agregar campo rechazada
//
//            // Estadísticas de solicitudes Organización
//            Map<String, Object> statsOrg = new HashMap<>();
//            statsOrg.put("pendientes", solAccesoOrgService.contarSolicitudesPorEstado(SolAccesoOrg.EstadoSolicitud.PENDIENTE));
//            statsOrg.put("aprobadas", solAccesoOrgService.contarSolicitudesPorEstado(SolAccesoOrg.EstadoSolicitud.APROBADA));
//            statsOrg.put("rechazadas", solAccesoOrgService.contarSolicitudesPorEstado(SolAccesoOrg.EstadoSolicitud.RECHAZADA));
//
//            estadisticas.put("solicitudesAPI", statsAPI);
//            estadisticas.put("solicitudesOrganizacion", statsOrg);
//            estadisticas.put("timestamp", System.currentTimeMillis());
//
//            return ResponseEntity.ok(estadisticas);
//
//        } catch (Exception e) {
//            Map<String, Object> error = new HashMap<>();
//            error.put("error", e.getMessage());
//            return ResponseEntity.status(500).body(error);
//        }
//    }
//
//    /**
//     * Obtener detalles de una solicitud específica (modal/popup)
//     */
//    @GetMapping("/api/solicitudes/api/{id}")
//    @ResponseBody
//    public ResponseEntity<Map<String, Object>> obtenerDetallesSolicitudAPI(@PathVariable Integer id) {
//        try {
//            SolicitudAccesoResponse solicitud = onboardingService.obtenerSolicitudPorId(id);
//
//            Map<String, Object> response = new HashMap<>();
//            response.put("solicitud", solicitud);
//            response.put("success", true);
//
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            Map<String, Object> error = new HashMap<>();
//            error.put("error", e.getMessage());
//            error.put("success", false);
//            return ResponseEntity.status(500).body(error);
//        }
//    }
//}