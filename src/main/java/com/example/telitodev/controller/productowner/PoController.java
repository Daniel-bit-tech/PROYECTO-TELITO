package com.example.telitodev.controller.productowner;

import com.example.telitodev.controller.BaseController;
import com.example.telitodev.entity.Api;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.entity.Notificacion;
import com.example.telitodev.entity.ActividadReciente; // Importa esta clase
import com.example.telitodev.repository.NotificacionRepository;
import com.example.telitodev.repository.UsuarioRepository;
import com.example.telitodev.service.ApiService;
import com.example.telitodev.service.NotificacionService;
import com.example.telitodev.service.ActividadRecienteService; // Importa el servicio
import com.example.telitodev.service.OnboardingService;
import com.example.telitodev.dto.SolicitudAccesoDecisionRequest;
import com.example.telitodev.dto.SolicitudAccesoResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/po")
public class PoController extends BaseController {

    final UsuarioRepository usuarioRepository;
    final ApiService apiService;
    final NotificacionService notificacionService;
    final ActividadRecienteService actividadRecienteService;
    final OnboardingService onboardingService;
    final NotificacionRepository notificacionRepository;

    public PoController(UsuarioRepository usuarioRepository, ApiService apiService, 
                       NotificacionService notificacionService, ActividadRecienteService actividadRecienteService,
                       OnboardingService onboardingService, NotificacionRepository notificacionRepository) {
        this.usuarioRepository = usuarioRepository;
        this.apiService = apiService;
        this.notificacionService = notificacionService;
        this.actividadRecienteService = actividadRecienteService;
        this.onboardingService = onboardingService;
        this.notificacionRepository = notificacionRepository;
    }
    
    @GetMapping("/Dashboard")
    public String showDashboardView(Model model, Authentication auth, HttpSession session) {
        // Obtener el usuario correcto considerando impersonación
        Usuario usuario = getCurrentUser(auth, session);
        model.addAttribute("usuario", usuario);
        
        // Agregar información de impersonación al modelo usando BaseController
        addImpersonationAttributes(model, session);
        
        return "po/home";
    }

    @GetMapping("/verPerfil")
    public String showverPerfilView(Model model, Authentication auth, HttpSession session) {
        // Obtener el usuario correcto considerando impersonación
        Usuario usuario = getCurrentUser(auth, session);
        model.addAttribute("usuario", usuario);
        
        // Agregar información de impersonación al modelo
        addImpersonationAttributes(model, session);
        
        return "po/verPerfil";
    }

    @GetMapping("/home")
    public String showHomeView(Model model, Authentication auth, HttpSession session) {
        List<Api> recentApis = apiService.getRecentApis();
        model.addAttribute("recentApis", recentApis);

        // Obtener el usuario correcto considerando impersonación usando BaseController
        Usuario usuario = getCurrentUser(auth, session);
        model.addAttribute("usuario", usuario);

        // Agregar información de impersonación al modelo
        addImpersonationAttributes(model, session);

        // --- INICIO: Lógica de Notificaciones Actualizada (Dashboard) ---
        // Buscando solo las 5 últimas notificaciones NO LEÍDAS
        List<Notificacion> notis = notificacionRepository.findTop5ByUsuarioDniAndLeidoOrderByFechaDesc(usuario.getDni(), false);
        // Contando la cantidad de notificaciones que no han sido leídas
        Integer Nnotis = notificacionRepository.countByUsuario_DniAndLeido(usuario.getDni(),false);
        // --- FIN: Lógica de Notificaciones Actualizada ---
        model.addAttribute("Nnotis", Nnotis);
        model.addAttribute("notificaciones", notis);



        List<ActividadReciente> actividadesRecientes = actividadRecienteService.obtenerActividadesRecientesPorUsuario(usuario.getDni());
        model.addAttribute("actividadesRecientes", actividadesRecientes);

        System.out.println("Notificaciones " + notificaciones);
        System.out.println("Actividades recientes " + actividadesRecientes);

        return "po/home";
    }

    /**
     * Endpoint para aprobar una solicitud de API
     */
    @PostMapping("/api/solicitud/{idSolicitud}/aprobar")
    @ResponseBody
    public ResponseEntity<?> aprobarSolicitud(@PathVariable Integer idSolicitud, 
                                            Authentication auth) {
        try {
            // Crear request de decisión
            SolicitudAccesoDecisionRequest decision = new SolicitudAccesoDecisionRequest();
            decision.setAccion("APROBAR");
            
            // Procesar la solicitud
            SolicitudAccesoResponse response = onboardingService.procesarSolicitud(idSolicitud, decision);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Solicitud aprobada exitosamente",
                "solicitud", response
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Endpoint para rechazar una solicitud de API
     */
    @PostMapping("/api/solicitud/{idSolicitud}/rechazar")
    @ResponseBody
    public ResponseEntity<?> rechazarSolicitud(@PathVariable Integer idSolicitud,
                                             @RequestParam(required = false) String motivo,
                                             Authentication auth) {
        try {
            // Crear request de decisión
            SolicitudAccesoDecisionRequest decision = new SolicitudAccesoDecisionRequest();
            decision.setAccion("RECHAZAR");
            decision.setMotivoRechazo(motivo);
            
            // Procesar la solicitud
            SolicitudAccesoResponse response = onboardingService.procesarSolicitud(idSolicitud, decision);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Solicitud rechazada exitosamente",
                "solicitud", response
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Endpoint para obtener todas las solicitudes pendientes de aprobación
     */
    @GetMapping("/api/solicitudes-pendientes")
    @ResponseBody
    public ResponseEntity<?> obtenerSolicitudesPendientes(Authentication auth) {
        try {
            // Obtener usuario PO actual
            Usuario po = usuarioRepository.findByCorreo(auth.getName());
            
            // Obtener solicitudes pendientes de su organización
            List<SolicitudAccesoResponse> solicitudesPendientes = 
                onboardingService.obtenerSolicitudesPendientesPorOrganizacion(po.getOrganizacion().getIdOrganizacion());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "solicitudes", solicitudesPendientes
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
}