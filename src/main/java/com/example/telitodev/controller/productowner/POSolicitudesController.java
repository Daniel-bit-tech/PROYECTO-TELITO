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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.telitodev.dto.SolicitudAccesoDecisionRequest;

@Controller
@RequestMapping("/po")
public class POSolicitudesController extends BaseController {

    @Autowired
    private OnboardingService onboardingService;

    @Autowired
    private UsuarioService usuarioService;


    @GetMapping("/solicitudes")
    public String mostrarSolicitudesPendientes(Model model, Authentication authentication, HttpSession session) {
        try {

            Usuario usuario = getCurrentUser(authentication, session);

            if (usuario == null) {
                return "redirect:/login";
            }


            boolean esPO = usuario.getRol().getNombreRol().equals("PO");
            boolean esSuperAdmin = usuario.getRol().getNombreRol().equals("SUPERADMIN");

            if (!esPO && !esSuperAdmin) {

                return "redirect:/home?error=no_permiso";
            }


            if (usuario.getEquipo() == null) {
                model.addAttribute("error", "Error crítico: Tu usuario PO no tiene un equipo asignado.");
                return "po/bandejaSolicitud";
            }

            addImpersonationAttributes(model, session);
            model.addAttribute("usuario", usuario);


            Integer idEquipoPO = usuario.getEquipo().getIdEquipo();

            List<SolicitudAccesoResponse> solicitudesPendientes =
                    onboardingService.obtenerSolicitudesPendientesPorEquipo(idEquipoPO);

            model.addAttribute("solicitudesPendientes", solicitudesPendientes);
            model.addAttribute("totalSolicitudes", solicitudesPendientes.size());

            return "po/bandejaSolicitud";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar las solicitudes: " + e.getMessage());
            return "po/bandejaSolicitud";
        }
    }


    @PostMapping("/solicitudes/{idSolicitud}/decision")
    @ResponseBody
    public ResponseEntity<?> procesarDecision(
            @PathVariable Integer idSolicitud,
            @RequestBody SolicitudAccesoDecisionRequest decision,
            Authentication authentication,
            HttpSession session) {
        
        try {
            // Verificar que el usuario sea PO
            Usuario usuario = getCurrentUser(authentication, session);
            if (usuario == null || (!usuario.getRol().getNombreRol().equals("PO") && !usuario.getRol().getNombreRol().equals("SUPERADMIN"))) {
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


    @GetMapping("/api/solicitudes-pendientes-all")
    @ResponseBody
    public ResponseEntity<List<SolicitudAccesoResponse>> obtenerSolicitudesPendientes(Authentication authentication, HttpSession session) {
        try {
            Usuario usuario = getCurrentUser(authentication, session);
            
            if (usuario == null || (!usuario.getRol().getNombreRol().equals("PO") && !usuario.getRol().getNombreRol().equals("SUPERADMIN"))) {
                return ResponseEntity.status(403).build();
            }

            List<SolicitudAccesoResponse> solicitudes = 
                onboardingService.obtenerSolicitudesPendientesPorOrganizacion(usuario.getOrganizacion().getIdOrganizacion());

            return ResponseEntity.ok(solicitudes);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }



    @PostMapping("/solicitudes/aprobar")
    public String aprobarSolicitud(@RequestParam("idSolicitud") Integer idSolicitud,
                                   Authentication authentication,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = getCurrentUser(authentication, session);

            if (usuario == null || !"PO".equals(usuario.getRol().getNombreRol())) {
                return "redirect:/login";
            }


            SolicitudAccesoDecisionRequest decision = new SolicitudAccesoDecisionRequest();
            decision.setAccion("APROBAR");


            onboardingService.procesarSolicitud(idSolicitud, decision);


            redirectAttributes.addFlashAttribute("mensaje", "Solicitud aprobada");

            return "redirect:/po/solicitudes";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al aprobar: " + e.getMessage());
            return "redirect:/po/solicitudes";
        }
    }

    @PostMapping("/solicitudes/rechazar")
    public String rechazarSolicitud(@RequestParam("idSolicitud") Integer idSolicitud,
                                    Authentication authentication,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = getCurrentUser(authentication, session);
            if (usuario == null) return "redirect:/login";

            SolicitudAccesoDecisionRequest decision = new SolicitudAccesoDecisionRequest();
            decision.setAccion("RECHAZAR");
            decision.setMotivoRechazo("Rechazado por el Product Owner");

            onboardingService.procesarSolicitud(idSolicitud, decision);

            redirectAttributes.addFlashAttribute("mensaje", "Solicitud rechazada");

            return "redirect:/po/solicitudes";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al rechazar: " + e.getMessage());
            return "redirect:/po/solicitudes";
        }
    }
}
