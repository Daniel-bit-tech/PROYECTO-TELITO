package com.example.telitodev.controller.desarrollador;

import com.example.telitodev.controller.BaseController;

import java.sql.Timestamp;
import java.util.List;

import com.example.telitodev.entity.*;
import com.example.telitodev.repository.*;
import com.example.telitodev.repository.SolicitudAccesoRepository;
import com.example.telitodev.repository.po.ActividadRecienteRepository;
import com.example.telitodev.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.telitodev.dto.ApiResponse;
import com.example.telitodev.dto.CredencialApiResponse;
import com.example.telitodev.dto.ErrorResponse;
import com.example.telitodev.dto.SolicitudAccesoRequest;
import com.example.telitodev.dto.SolicitudAccesoResponse;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.UsuarioRepository;
import com.example.telitodev.service.OnboardingService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
public class OnboardingController extends BaseController {

    final UsuarioRepository usuarioRepository;
    final OnboardingService onboardingService;
    final SolicitudAccesoRepository solicitudAccesoRepository;
    final NotificacionRepository notificacionRepository;
    final UsuarioService usuarioService;
    final ActividadRecienteRepository actividadRecienteRepository;

    public OnboardingController(UsuarioRepository usuarioRepository, OnboardingService onboardingService,
                                SolicitudAccesoRepository solicitudAccesoRepository, NotificacionRepository notificacionRepository, UsuarioService usuarioService,
                                UsuarioRepository userRepository, ActividadRecienteRepository actividadRecienteRepository) {
        this.usuarioRepository = usuarioRepository;
        this.onboardingService = onboardingService;
        this.solicitudAccesoRepository = solicitudAccesoRepository;
        this.notificacionRepository = notificacionRepository;
        this.usuarioService = usuarioService;
        this.actividadRecienteRepository = actividadRecienteRepository;
    }

    @GetMapping("/onboarding")
    public String showonb(Model model, Authentication auth, HttpSession session) {
        // Obtener el usuario correcto considerando impersonación usando BaseController
        Usuario usuario = getCurrentUser(auth, session);

        if (usuario == null) {
            throw new RuntimeException("Usuario no encontrado");
        }

        // Validar que el usuario tenga rol DEV
        if (usuario.getRol().getIdRol() != 2) {
            return "redirect:/access-denied"; // Redirigir si no es DEV
        }

        model.addAttribute("usuario", usuario);

        // Agregar información de impersonación al modelo usando BaseController
        addImpersonationAttributes(model, session);

        // Verificar si el usuario tiene organización asignada
        if (usuario.getOrganizacion() == null) {
            // Usuario nuevo sin organización - mostrar vista de bienvenida básica
            model.addAttribute("usuarioNuevo", true);
            model.addAttribute("apisDisponibles", List.of());
            model.addAttribute("misCredenciales", List.of());
            model.addAttribute("totalCredenciales", 0);
            model.addAttribute("credencialesActivas", 0L);
            model.addAttribute("mensaje", "¡Bienvenido! Tu cuenta está siendo configurada. Pronto tendrás acceso a nuestras APIs.");
        } else {
            // Usuario con organización - cargar datos normalmente
            model.addAttribute("usuarioNuevo", false);
            try {
                //List<ApiResponse> apisDisponibles = onboardingService.obtenerApisDisponibles(usuario.getDni());
                List<CredencialApiResponse> misCredenciales = onboardingService.obtenerCredencialesUsuario(usuario.getDni());

                //model.addAttribute("apisDisponibles", apisDisponibles);
                model.addAttribute("misCredenciales", misCredenciales);
                model.addAttribute("totalCredenciales", misCredenciales.size());

                long credencialesActivas = misCredenciales.stream()
                        .filter(CredencialApiResponse::getEstado)
                        .count();
                model.addAttribute("credencialesActivas", credencialesActivas);

            } catch (Exception e) {
                // En caso de error, continuar con la vista pero sin los datos adicionales
                model.addAttribute("error", "Error al cargar datos del onboarding");
            }
        }

        return "desarrollador/onboarding";
    }

    /**
     * Endpoint REST para obtener APIs disponibles (consumido por JavaScript)
     */
    @GetMapping("/api/onboarding/apis")
    @ResponseBody
    public ResponseEntity<List<ApiResponse>> getApisDisponibles(Authentication auth) {
        try {
            Usuario usuario = usuarioRepository.findByCorreo(auth.getName());

            if (usuario == null) {
                return ResponseEntity.status(401).build();
            }

            // Validar que el usuario tenga rol DEV
            if (usuario.getRol().getIdRol() != 2) {
                return ResponseEntity.status(403).build();
            }

            // Si el usuario no tiene organización, devolver lista vacía
            if (usuario.getOrganizacion() == null) {
                return ResponseEntity.ok(List.of());
            }

            List<ApiResponse> apisDisponibles = onboardingService.obtenerApisDisponibles(usuario.getDni());
            return ResponseEntity.ok(apisDisponibles);

        } catch (Exception e) {
            System.err.println("Error al obtener APIs disponibles: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Endpoint REST para obtener credenciales del usuario (consumido por JavaScript)
     */
    @GetMapping(value = "/api/onboarding/mis-credenciales", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getMisCredenciales(Authentication auth) throws Exception {
        try {
            Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
            if (usuario == null) {
                return "{\"error\":\"Usuario no autorizado\"}";
            }

            // Si el usuario no tiene organización, devolver lista vacía
            if (usuario.getOrganizacion() == null) {
                return "[]";
            }

            List<CredencialApiResponse> misCredenciales = onboardingService.obtenerCredencialesUsuario(usuario.getDni());

            // --- CONVERSIÓN MANUAL A JSON ---
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(misCredenciales);
            // --------------------------------

        } catch (Exception e) {
            System.err.println("Error al obtener credenciales del usuario: " + e.getMessage());
            return "{\"error\":\"Error interno del servidor\"}";
        }
    }


    /**
     * Endpoint REST para revocar una credencial (consumido por JavaScript)
     */
    @DeleteMapping("/api/onboarding/credenciales/{credencialId}")
    @ResponseBody
    public ResponseEntity<?> revocarCredencial(@PathVariable Integer credencialId, Authentication auth) {
        try {
            Usuario usuario = usuarioRepository.findByCorreo(auth.getName());

            if (usuario == null) {
                return ResponseEntity.status(401).body("{\"error\":\"Usuario no encontrado\"}");
            }

            // Validar que el usuario tenga rol DEV
            if (usuario.getRol().getIdRol() != 2) {
                return ResponseEntity.status(403).body("{\"error\":\"No tienes permisos para realizar esta acción\"}");
            }

            // Revocar la credencial usando el servicio
            onboardingService.revocarCredencial(credencialId, usuario.getDni());

            return ResponseEntity.ok("{\"message\":\"Credencial revocada exitosamente\"}");

        } catch (RuntimeException e) {
            System.err.println("Error al revocar credencial: " + e.getMessage());
            return ResponseEntity.status(400).body("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            System.err.println("Error interno al revocar credencial: " + e.getMessage());
            return ResponseEntity.status(500).body("{\"error\":\"Error interno del servidor\"}");
        }
    }

    /**
     * Endpoint REST para crear una nueva solicitud de acceso (consumido por JavaScript)
     */
    @PostMapping("/api/onboarding/solicitudes")
    @ResponseBody
    public ResponseEntity<?> crearSolicitudAcceso(@RequestBody SolicitudAccesoRequest solicitudRequest, Authentication auth) {
        try {
            Usuario usuario = usuarioRepository.findByCorreo(auth.getName());

            if (usuario == null) {
                return ResponseEntity.status(401).body(new ErrorResponse("Usuario no encontrado"));
            }

            // Validar que el usuario tenga rol DEV
            if (usuario.getRol().getIdRol() != 2) {
                return ResponseEntity.status(403).body(new ErrorResponse("No tienes permisos para solicitar acceso a APIs"));
            }

            // Crear la solicitud usando el servicio
            SolicitudAccesoResponse response = onboardingService.crearSolicitudAcceso(usuario.getDni(), solicitudRequest);

            // Obtener la solicitud creada para la notificación
            SolicitudAcceso solicitudCreada = solicitudAccesoRepository.findById(response.getIdSolicitudAcceso())
                    .orElseThrow(() -> new RuntimeException("Solicitud no encontrada después de creación"));

            // === NOTIFICACIÓN PARA EL PO ===
            // USAR LA ORGANIZACIÓN DEL DEV QUE ENVÍA LA SOLICITUD
            if (usuario.getOrganizacion() != null) {

                // Buscar al PO de la organización del DEV
                Usuario po = usuarioRepository.findPoByOrganizacion(usuario.getOrganizacion().getIdOrganizacion());

                if (po != null) {
                    // Crear notificación para el PO
                    Notificacion notificacion = new Notificacion();
                    notificacion.setMensaje("Solicitud de API Key: El desarrollador " + usuario.getNombre() + " " + usuario.getApellidoPaterno() +
                            " ha solicitado acceso a la: " + solicitudCreada.getApi().getNombre());
                    notificacion.setLeido(false);
                    notificacion.setFecha(new Timestamp(System.currentTimeMillis()));
                    notificacion.setUsuario(po);

                    notificacionRepository.save(notificacion);

                    System.out.println("✅ Notificación de solicitud API Key enviada al PO: " + po.getCorreo());
                    System.out.println("   Organización: " + usuario.getOrganizacion().getNombre());
                } else {
                    System.out.println("⚠️ No se encontró PO en la organización: " + usuario.getOrganizacion().getNombre());
                }
            } else {
                System.out.println("ℹ️ El DEV no tiene organización asignada - No se envía notificación");
            }

            return ResponseEntity.ok(response);

        } catch (com.example.telitodev.exception.ApiYaActivaException e) {
            System.err.println("API ya activa: " + e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            return ResponseEntity.status(400).body(errorResponse);
        } catch (RuntimeException e) {
            System.err.println("Error al crear solicitud de acceso: " + e.getMessage());
            e.printStackTrace();
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            return ResponseEntity.status(400).body(errorResponse);
        } catch (Exception e) {
            System.err.println("Error interno al crear solicitud: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ErrorResponse("Error interno del servidor"));
        }
    }


    @GetMapping("/api/onboarding/solicitudes-pendientes")
    @ResponseBody
    public ResponseEntity<List<SolicitudAccesoResponse>> getSolicitudesPendientes(Authentication auth) {
        try {
            Usuario usuario = usuarioRepository.findByCorreo(auth.getName());

            if (usuario == null) {
                return ResponseEntity.status(401).build();
            }


            if (usuario.getRol().getIdRol() != 2) {
                return ResponseEntity.status(403).build();
            }

            List<SolicitudAccesoResponse> solicitudesPendientes = onboardingService.obtenerSolicitudesPorUsuario(usuario.getDni());
            return ResponseEntity.ok(solicitudesPendientes);

        } catch (Exception e) {
            System.err.println("Error al obtener solicitudes pendientes: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }


    @GetMapping("/api/onboarding/mis-solicitudes")
    @ResponseBody
    public ResponseEntity<List<SolicitudAccesoResponse>> getMisSolicitudes(Authentication auth) {
        try {
            Usuario usuario = usuarioRepository.findByCorreo(auth.getName());

            if (usuario == null) {
                return ResponseEntity.status(401).build();
            }


            List<SolicitudAccesoResponse> misSolicitudes = onboardingService.obtenerSolicitudesPorUsuario(usuario.getDni());

            System.out.println("ONBOARDING: Enviando " + misSolicitudes.size() + " solicitudes para " + usuario.getDni());

            return ResponseEntity.ok(misSolicitudes);

        } catch (Exception e) {
            System.err.println("Error al obtener mis solicitudes: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}
