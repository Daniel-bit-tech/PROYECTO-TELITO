package com.example.telitodev.service;

import com.example.telitodev.dto.*;
import com.example.telitodev.entity.*;
import com.example.telitodev.repository.*;
import com.example.telitodev.exception.ApiYaActivaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class OnboardingService {

    private final SolicitudAccesoRepository solicitudAccesoRepository;
    private final CredencialApiRepository credencialApiRepository;
    private final ApiRepository apiRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacionRepository notificacionRepository;

    public OnboardingService(SolicitudAccesoRepository solicitudAccesoRepository,
                           CredencialApiRepository credencialApiRepository,
                           ApiRepository apiRepository,
                           UsuarioRepository usuarioRepository,
                           NotificacionRepository notificacionRepository) {
        this.solicitudAccesoRepository = solicitudAccesoRepository;
        this.credencialApiRepository = credencialApiRepository;
        this.apiRepository = apiRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacionRepository = notificacionRepository;
    }

    /**
     * Crear una nueva solicitud de acceso a una API
     */
    public SolicitudAccesoResponse crearSolicitudAcceso(String dniUsuario, SolicitudAccesoRequest request) {
        // Validar que el usuario existe
        Usuario usuario = usuarioRepository.findByDni(dniUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Validar que el usuario tenga rol DEV
        if (usuario.getRol().getIdRol() != 2) {
            throw new RuntimeException("Solo los usuarios con rol DEV pueden solicitar acceso a APIs");
        }

        // Validar que la API existe
        Api api = apiRepository.findById(request.getApiId())
                .orElseThrow(() -> new RuntimeException("API no encontrada"));

        // Verificar si ya existe una solicitud pendiente para esta API
        Optional<SolicitudAcceso> solicitudExistente = solicitudAccesoRepository
                .findSolicitudPendienteByUsuarioAndApi(dniUsuario, request.getApiId());

        if (solicitudExistente.isPresent()) {
            throw new RuntimeException("Ya tienes una solicitud pendiente para esta API. Puedes verificar su estado en 'Mis API Keys'.");
        }

        // Verificar si ya tiene acceso a esta API
        List<CredencialApi> credencialesExistentes = credencialApiRepository
                .findByUsuario_DniAndApi_IdApiAndEstado(dniUsuario, request.getApiId(), true);
        
        if (!credencialesExistentes.isEmpty()) {
            // En lugar de lanzar una excepción, crear una excepción personalizada
            throw new ApiYaActivaException("Esta API ya está en tus keys activas. Puedes verificarlo en la sección 'Mis API Keys'.");
        }

        // Crear la solicitud
        SolicitudAcceso solicitud = new SolicitudAcceso();
        solicitud.setUsuario(usuario);
        solicitud.setApi(api);
        solicitud.setEstado(false); // false = pendiente, true = aprobada
        solicitud.setFechaSolicitud(Timestamp.from(Instant.now()));
        solicitud.setNombreProyecto(request.getNombreProyecto());
        solicitud.setDescripcionUso(request.getDescripcionUso());
        solicitud.setEntorno(request.getEntorno());
        solicitud.setCallbackUrl(request.getCallbackUrl());

        SolicitudAcceso solicitudGuardada = solicitudAccesoRepository.save(solicitud);

        // Aprobar automáticamente la solicitud
        aprobarSolicitudAutomaticamente(solicitudGuardada.getIdSolicitudAcceso());

        return mapearASolicitudAccesoResponse(solicitudGuardada);
    }

    /**
     * Obtener todas las solicitudes de un usuario
     */
    public List<SolicitudAccesoResponse> obtenerSolicitudesUsuario(String dniUsuario) {
        List<SolicitudAcceso> solicitudes = solicitudAccesoRepository.findByUsuario_DniOrderByFechaSolicitudDesc(dniUsuario);
        return solicitudes.stream()
                .map(this::mapearASolicitudAccesoResponse)
                .collect(Collectors.toList());
    }

    /**
     * Aprobar o rechazar una solicitud
     */
    public SolicitudAccesoResponse procesarSolicitud(Integer idSolicitud, SolicitudAccesoDecisionRequest decision) {
        SolicitudAcceso solicitud = solicitudAccesoRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        // Verificar si la solicitud ya fue procesada (false = pendiente, true = ya procesada)
        if (solicitud.getEstado() == true) {
            throw new RuntimeException("Esta solicitud ya fue procesada");
        }

        boolean aprobada = "APROBAR".equalsIgnoreCase(decision.getAccion());
        solicitud.setEstado(true); // Marcar como procesada (true = procesada)
        solicitud.setFechaRespuesta(Timestamp.from(Instant.now()));

        if (aprobada) {
            // Generar credencial API
            generarCredencialApi(solicitud.getUsuario(), solicitud.getApi());
            
            // Enviar notificación de aprobación
            enviarNotificacion(solicitud.getUsuario(), 
                "Solicitud aprobada", 
                "Tu solicitud de acceso a " + solicitud.getApi().getNombre() + " ha sido aprobada. Ya puedes obtener tu API Key.");
        } else {
            // Enviar notificación de rechazo
            String mensaje = "Tu solicitud de acceso a " + solicitud.getApi().getNombre() + " ha sido rechazada.";
            if (decision.getMotivoRechazo() != null && !decision.getMotivoRechazo().trim().isEmpty()) {
                mensaje += " Motivo: " + decision.getMotivoRechazo();
            }
            enviarNotificacion(solicitud.getUsuario(), "Solicitud rechazada", mensaje);
        }

        SolicitudAcceso solicitudActualizada = solicitudAccesoRepository.save(solicitud);
        return mapearASolicitudAccesoResponse(solicitudActualizada);
    }

    /**
     * Obtener credenciales activas de un usuario
     */
    public List<CredencialApiResponse> obtenerCredencialesUsuario(String dniUsuario) {
        List<CredencialApi> credenciales = credencialApiRepository.findByUsuario_DniOrderByFechaCreacionDesc(dniUsuario);
        return credenciales.stream()
                .map(this::mapearACredencialApiResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener todas las APIs disponibles
     */
    public List<ApiResponse> obtenerApisDisponibles() {
        List<Api> apis = apiRepository.findAll();
        return apis.stream()
                .map(this::mapearAApiResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener DNI del usuario por correo electrónico
     */
    public String obtenerDniPorCorreo(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo);
        if (usuario == null) {
            throw new RuntimeException("Usuario no encontrado");
        }
        return usuario.getDni();
    }

    /**
     * Revocar una credencial de API (cambiar estado a inactivo)
     */
    public void revocarCredencial(Integer credencialId, String dniUsuario) {
        // Buscar la credencial
        CredencialApi credencial = credencialApiRepository.findById(credencialId)
                .orElseThrow(() -> new RuntimeException("Credencial no encontrada"));

        // Verificar que la credencial pertenece al usuario
        if (!credencial.getUsuario().getDni().equals(dniUsuario)) {
            throw new RuntimeException("No tienes permisos para revocar esta credencial");
        }

        // Verificar que la credencial esté activa
        if (!credencial.getEstado()) {
            throw new RuntimeException("La credencial ya está inactiva");
        }

        // Revocar la credencial (cambiar estado a false)
        credencial.setEstado(false);
        
        // Guardar los cambios
        credencialApiRepository.save(credencial);

        // Crear notificación de revocación
        enviarNotificacion(
            credencial.getUsuario(),
            "Credencial Revocada",
            "Tu credencial para la API '" + credencial.getApi().getNombre() + "' ha sido revocada exitosamente."
        );
    }

    // Métodos privados

    private void aprobarSolicitudAutomaticamente(Integer idSolicitud) {
        SolicitudAccesoDecisionRequest decision = new SolicitudAccesoDecisionRequest("APROBAR", null);
        procesarSolicitud(idSolicitud, decision);
    }

    private void generarCredencialApi(Usuario usuario, Api api) {
        // Generar API Key única
        String apiKey = "APIKEY-" + usuario.getDni().substring(4) + "-A" + api.getIdApi() + "-" + 
                        UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        CredencialApi credencial = new CredencialApi();
        credencial.setApiKey(apiKey);
        credencial.setUsuario(usuario);
        credencial.setApi(api);
        credencial.setEstado(true);
        credencial.setFechaCreacion(Timestamp.from(Instant.now()));

        credencialApiRepository.save(credencial);
    }

    private void enviarNotificacion(Usuario usuario, String asunto, String mensaje) {
        Notificacion notificacion = new Notificacion();
        // No establecemos el ID, se generará automáticamente con AUTO_INCREMENT
        notificacion.setUsuario(usuario);
        notificacion.setMensaje(asunto + ": " + mensaje);
        notificacion.setLeido(false);
        notificacion.setFecha(Timestamp.from(Instant.now()));

        notificacionRepository.save(notificacion);
    }

    // Métodos de mapeo

    private SolicitudAccesoResponse mapearASolicitudAccesoResponse(SolicitudAcceso solicitud) {
        String estado = "PENDIENTE";
        if (solicitud.getEstado() != null) {
            estado = solicitud.getEstado() ? "APROBADA" : "RECHAZADA";
        }

        return new SolicitudAccesoResponse(
                solicitud.getIdSolicitudAcceso(),
                solicitud.getApi().getNombre(),
                solicitud.getApi().getIdApi(),
                estado,
                solicitud.getFechaSolicitud(),
                solicitud.getFechaRespuesta(),
                solicitud.getNombreProyecto() != null ? solicitud.getNombreProyecto() : "",
                solicitud.getDescripcionUso() != null ? solicitud.getDescripcionUso() : ""
        );
    }

    private CredencialApiResponse mapearACredencialApiResponse(CredencialApi credencial) {
        String estadoTexto = credencial.getEstado() ? "Activa" : "Inactiva";

        return new CredencialApiResponse(
                credencial.getIdCredencialApi(),
                credencial.getApiKey(),
                credencial.getApi().getNombre(),
                credencial.getApi().getIdApi(),
                credencial.getFechaCreacion(),
                credencial.getEstado(),
                estadoTexto
        );
    }

    private ApiResponse mapearAApiResponse(Api api) {
        return new ApiResponse(
                api.getIdApi(),
                api.getNombre(),
                api.getDescripcion(),
                api.getDominio(),
                api.getTag(),
                api.getEndpointUrl()
        );
    }
}