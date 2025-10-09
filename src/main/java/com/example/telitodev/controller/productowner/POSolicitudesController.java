package com.example.telitodev.controller.productowner;

import com.example.telitodev.controller.BaseController;
import com.example.telitodev.dto.SolicitudAccesoDecisionRequest;
import com.example.telitodev.dto.SolicitudAccesoResponse;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.service.OnboardingService;
import com.example.telitodev.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/po")
public class POSolicitudesController extends BaseController {

    @Autowired
    private OnboardingService onboardingService;

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Vista principal de solicitudes pendientes para PO
     */
    @GetMapping("/solicitudes")
    public String mostrarSolicitudesPendientes(Model model, Authentication authentication, HttpSession session) {
        try {
            // Obtener el usuario autenticado considerando impersonación
            Usuario usuario = getCurrentUser(authentication, session);
            
            if (usuario == null) {
                return "redirect:/login";
            }

            // Validar que el usuario tenga rol PO
            if (usuario.getRol().getIdRol() != 1) {
                return "redirect:/access-denied";
            }

            // Agregar atributos de impersonación
            addImpersonationAttributes(model, session);

            model.addAttribute("usuario", usuario);

            // Obtener solicitudes pendientes para esta organización
            List<SolicitudAccesoResponse> solicitudesPendientes = 
                onboardingService.obtenerSolicitudesPendientesPorOrganizacion(usuario.getOrganizacion().getIdOrganizacion());

            model.addAttribute("solicitudesPendientes", solicitudesPendientes);
            model.addAttribute("totalSolicitudes", solicitudesPendientes.size());

            return "po/solicitudes";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar las solicitudes: " + e.getMessage());
            return "po/solicitudes";
        }
    }

    /**
     * Procesar decisión sobre una solicitud (aprobar/rechazar)
     */
    @PostMapping("/solicitudes/{idSolicitud}/decision")
    @ResponseBody
    public ResponseEntity<?> procesarDecision(
            @PathVariable Integer idSolicitud,
            @RequestBody SolicitudAccesoDecisionRequest decision,
            Authentication authentication) {
        
        try {
            // Verificar que el usuario sea PO
            Usuario usuario = usuarioService.findByCorreo(authentication.getName());
            if (usuario == null || usuario.getRol().getIdRol() != 1) {
                return ResponseEntity.status(403).body("No tienes permisos para realizar esta acción");
            }

            // Procesar la solicitud
            SolicitudAccesoResponse solicitudProcesada = 
                onboardingService.procesarSolicitud(idSolicitud, decision);

            return ResponseEntity.ok(solicitudProcesada);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al procesar la solicitud: " + e.getMessage());
        }
    }

    /**
     * API REST para obtener solicitudes pendientes (para actualizaciones dinámicas)
     */
    @GetMapping("/api/solicitudes-pendientes-all")
    @ResponseBody
    public ResponseEntity<List<SolicitudAccesoResponse>> obtenerSolicitudesPendientes(Authentication authentication) {
        try {
            Usuario usuario = usuarioService.findByCorreo(authentication.getName());
            
            if (usuario == null || usuario.getRol().getIdRol() != 1) {
                return ResponseEntity.status(403).build();
            }

            List<SolicitudAccesoResponse> solicitudes = 
                onboardingService.obtenerSolicitudesPendientesPorOrganizacion(usuario.getOrganizacion().getIdOrganizacion());

            return ResponseEntity.ok(solicitudes);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
