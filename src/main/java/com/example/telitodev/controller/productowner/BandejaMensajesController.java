package com.example.telitodev.controller.productowner;

import com.example.telitodev.dto.SolicitudAccesoDecisionRequest;
import com.example.telitodev.dto.SolicitudAccesoResponse;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.UsuarioRepository;
import com.example.telitodev.service.OnboardingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/po")
@PreAuthorize("hasAnyRole('PO', 'SADMIN')")
public class BandejaMensajesController {

    final UsuarioRepository usuarioRepository;
    final OnboardingService onboardingService;

    public BandejaMensajesController(UsuarioRepository usuarioRepository, OnboardingService onboardingService) {
        this.usuarioRepository = usuarioRepository;
        this.onboardingService = onboardingService;
    }

    @GetMapping("/bandejaSolicitud")
    public String showbandejaSolicitudesView(Model model, Authentication auth) {
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);

        // Obtener solicitudes pendientes de la organización del PO
        if (usuario != null && usuario.getOrganizacion() != null) {
            try {
                List<SolicitudAccesoResponse> solicitudesPendientes =
                        onboardingService.obtenerSolicitudesPendientesPorOrganizacion(usuario.getOrganizacion().getIdOrganizacion());
                model.addAttribute("solicitudesPendientes", solicitudesPendientes);
                System.out.println("BANDEJA: Solicitudes encontradas: " + solicitudesPendientes.size());

                // Debug: mostrar cada solicitud
                for (SolicitudAccesoResponse sol : solicitudesPendientes) {
                    System.out.println("  - Solicitud ID: " + sol.getIdSolicitudAcceso() +
                            ", Proyecto: " + sol.getNombreProyecto() +
                            ", Estado: " + sol.getEstado());
                }
            } catch (Exception e) {
                System.err.println("Error al obtener solicitudes: " + e.getMessage());
                e.printStackTrace();
                model.addAttribute("solicitudesPendientes", null);
            }
        } else {
            System.err.println("Usuario sin organización o nulo");
            model.addAttribute("solicitudesPendientes", null);
        }

        return "po/bandejaSolicitud";
    }

    @GetMapping("/verSolicitud")
    public String showSolicitudesView(@RequestParam("id") Integer idSolicitud, Model model, Authentication auth){
        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);

        try {
            // Obtener la solicitud específica por ID
            SolicitudAccesoResponse solicitud = onboardingService.obtenerSolicitudPorId(idSolicitud);

            // Verificar que la solicitud pertenece a la organización del PO
            if (usuario.getOrganizacion() == null) {
                model.addAttribute("error", "Usuario sin organización asignada");
                return "po/verSolicitud";
            }

            model.addAttribute("solicitud", solicitud);
            System.out.println("VER SOLICITUD: Cargando solicitud ID: " + idSolicitud);

        } catch (Exception e) {
            System.err.println("Error al cargar solicitud: " + e.getMessage());
            model.addAttribute("error", "No se pudo cargar la solicitud");
        }

        return "po/verSolicitud";
    }

    @PostMapping("/aprobarSolicitud")
    public String aprobarSolicitud(@RequestParam("id") Integer idSolicitud, Authentication auth) {
        try {
            // Crear objeto de decisión para aprobar
            SolicitudAccesoDecisionRequest decision = new SolicitudAccesoDecisionRequest();
            decision.setAccion("APROBAR");

            // Procesar la solicitud
            onboardingService.procesarSolicitud(idSolicitud, decision);

            System.out.println("APROBADA: Solicitud " + idSolicitud + " aprobada exitosamente");

        } catch (Exception e) {
            System.err.println("Error al aprobar solicitud: " + e.getMessage());
        }

        return "redirect:/po/bandejaSolicitud";
    }

    @PostMapping("/rechazarSolicitud")
    public String rechazarSolicitud(@RequestParam("id") Integer idSolicitud,
                                    @RequestParam(value = "motivo", required = false) String motivo,
                                    Authentication auth) {
        try {
            // Crear objeto de decisión para rechazar
            SolicitudAccesoDecisionRequest decision = new SolicitudAccesoDecisionRequest();
            decision.setAccion("RECHAZAR");
            decision.setMotivoRechazo(motivo);

            // Procesar la solicitud
            onboardingService.procesarSolicitud(idSolicitud, decision);

            System.out.println("RECHAZADA: Solicitud " + idSolicitud + " rechazada exitosamente");

        } catch (Exception e) {
            System.err.println("Error al rechazar solicitud: " + e.getMessage());
        }

        return "redirect:/po/bandejaSolicitud";
    }

}
