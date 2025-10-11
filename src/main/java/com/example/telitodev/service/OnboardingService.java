package com.example.telitodev.service;

import com.example.telitodev.dto.*;
import com.example.telitodev.entity.*;
import com.example.telitodev.repository.*;
import com.example.telitodev.exception.ApiYaActivaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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
    private final DominioRepository dominioRepository;
    private final TagRepository tagRepository;
    private final ApiHasEntornoRepository apiHasEntornoRepository;



    public OnboardingService(SolicitudAccesoRepository solicitudAccesoRepository,
                             CredencialApiRepository credencialApiRepository,
                             ApiRepository apiRepository,
                             UsuarioRepository usuarioRepository,
                             NotificacionRepository notificacionRepository,
                             DominioRepository dominioRepository,
                             TagRepository tagRepository, ApiHasEntornoRepository apiHasEntornoRepository) {
        this.solicitudAccesoRepository = solicitudAccesoRepository;
        this.credencialApiRepository = credencialApiRepository;
        this.apiRepository = apiRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacionRepository = notificacionRepository;
        this.dominioRepository = dominioRepository;
        this.tagRepository = tagRepository;
        this.apiHasEntornoRepository = apiHasEntornoRepository;
    }



    public SolicitudAccesoResponse crearSolicitudAcceso(String dniUsuario, SolicitudAccesoRequest request) {
        Usuario usuario = usuarioRepository.findByDni(dniUsuario);
        if (usuario == null) {
            throw new RuntimeException("Usuario no encontrado");
        }

        if (usuario.getRol().getIdRol() != 2) {
            throw new RuntimeException("Solo los usuarios con rol DEV pueden solicitar acceso a APIs");
        }

        if (usuario.getOrganizacion() == null) {
            throw new RuntimeException("Tu cuenta no tiene una organización asignada. Contacta al administrador para que te asigne a una organización.");
        }

        Api api = apiRepository.findById(request.getApiId())
                .orElseThrow(() -> new RuntimeException("API no encontrada"));

        Optional<SolicitudAcceso> solicitudExistente = solicitudAccesoRepository
                .findSolicitudPendienteByUsuarioAndApi(dniUsuario, request.getApiId());

        if (solicitudExistente.isPresent()) {
            throw new RuntimeException("Ya tienes una solicitud pendiente para esta API. Puedes verificar su estado en 'Mis API Keys'.");
        }

        List<CredencialApi> credencialesExistentes = credencialApiRepository
                .findByUsuario_DniAndApi_IdApiAndEstado(dniUsuario, request.getApiId(), true);

        for (CredencialApi credencial : credencialesExistentes) {
            System.out.println(credencial.getIdCredencialApi());
        }

        if (!credencialesExistentes.isEmpty()) {
            throw new ApiYaActivaException("Esta API ya está en tus keys activas. Puedes verificarlo en la sección 'Mis API Keys'.");
        }

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

        notificarPOSobreSolicitud(solicitudGuardada);

        return mapearASolicitudAccesoResponse(solicitudGuardada);
    }


    public List<SolicitudAccesoResponse> obtenerSolicitudesUsuario(String dniUsuario) {
        List<SolicitudAcceso> solicitudes = solicitudAccesoRepository.findByUsuario_DniOrderByFechaSolicitudDesc(dniUsuario);
        return solicitudes.stream()
                .map(this::mapearASolicitudAccesoResponse)
                .collect(Collectors.toList());
    }


    public List<SolicitudAccesoResponse> obtenerSolicitudesPendientesPorOrganizacion(Integer idOrganizacion) {
        List<SolicitudAcceso> solicitudes = solicitudAccesoRepository
                .findByEstadoAndUsuario_Organizacion_IdOrganizacionOrderByFechaSolicitudDesc(false, idOrganizacion);
        return solicitudes.stream()
                .map(this::mapearASolicitudAccesoResponse)
                .collect(Collectors.toList());
    }


    public SolicitudAccesoResponse obtenerSolicitudPorId(Integer idSolicitud) {
        SolicitudAcceso solicitud = solicitudAccesoRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        return mapearASolicitudAccesoResponse(solicitud);
    }


    public SolicitudAccesoResponse procesarSolicitud(Integer idSolicitud, SolicitudAccesoDecisionRequest decision) {
        SolicitudAcceso solicitud = solicitudAccesoRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (solicitud.getEstado() == true) {
            throw new RuntimeException("Esta solicitud ya fue procesada");
        }

        boolean aprobada = "APROBAR".equalsIgnoreCase(decision.getAccion());
        solicitud.setEstado(true);
        solicitud.setFechaRespuesta(Timestamp.from(Instant.now()));

        if (aprobada) {
            generarCredencialApi(solicitud.getUsuario(), solicitud.getApi());

            enviarNotificacion(solicitud.getUsuario(),
                    "Solicitud aprobada",
                    "Tu solicitud de acceso a " + solicitud.getApi().getNombre() + " ha sido aprobada. Ya puedes obtener tu API Key.");
        } else {
            String mensaje = "Tu solicitud de acceso a " + solicitud.getApi().getNombre() + " ha sido rechazada.";
            if (decision.getMotivoRechazo() != null && !decision.getMotivoRechazo().trim().isEmpty()) {
                mensaje += " Motivo: " + decision.getMotivoRechazo();
            }
            enviarNotificacion(solicitud.getUsuario(), "Solicitud rechazada", mensaje);
        }

        SolicitudAcceso solicitudActualizada = solicitudAccesoRepository.save(solicitud);
        return mapearASolicitudAccesoResponse(solicitudActualizada);
    }


    public List<CredencialApiResponse> obtenerCredencialesUsuario(String dniUsuario) {
        List<CredencialApi> credenciales = credencialApiRepository.findByUsuario_DniOrderByFechaCreacionDesc(dniUsuario);
        return credenciales.stream()
                .map(this::mapearACredencialApiResponse)
                .collect(Collectors.toList());
    }

    public List<ApiResponse> obtenerApisDisponibles() {
        List<Api> apis = apiRepository.findAll();
        return apis.stream()
                .map(this::mapearAApiResponse)
                .collect(Collectors.toList());
    }

    public String obtenerDniPorCorreo(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo);
        if (usuario == null) {
            throw new RuntimeException("Usuario no encontrado");
        }
        return usuario.getDni();
    }


    public void revocarCredencial(Integer credencialId, String dniUsuario) {
        CredencialApi credencial = credencialApiRepository.findById(credencialId)
                .orElseThrow(() -> new RuntimeException("Credencial no encontrada"));

        if (!credencial.getUsuario().getDni().equals(dniUsuario)) {
            throw new RuntimeException("No tienes permisos para revocar esta credencial");
        }

        if (!credencial.getEstado()) {
            throw new RuntimeException("La credencial ya está inactiva");
        }

        credencial.setEstado(false);

        credencialApiRepository.save(credencial);

        enviarNotificacion(
                credencial.getUsuario(),
                "Credencial Revocada",
                "Tu credencial para la API '" + credencial.getApi().getNombre() + "' ha sido revocada exitosamente."
        );
    }




    private void generarCredencialApi(Usuario usuario, Api api) {
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
        System.out.println("DEBUG: Creando notificación para usuario: " + usuario.getCorreo());
        System.out.println("DEBUG: Asunto: " + asunto);

        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(usuario);
        notificacion.setMensaje(asunto + ": " + mensaje);
        notificacion.setLeido(false);
        notificacion.setFecha(Timestamp.from(Instant.now()));

        Notificacion notificacionGuardada = notificacionRepository.save(notificacion);
        System.out.println("DEBUG: Notificación guardada con ID: " + notificacionGuardada.getIdNotificacion());
    }


    private void notificarPOSobreSolicitud(SolicitudAcceso solicitud) {
        if (solicitud.getUsuario().getOrganizacion() == null) {
            System.err.println("No se puede notificar al PO: el usuario no tiene organización asignada");
            return;
        }

        Integer organizacionId = solicitud.getUsuario().getOrganizacion().getIdOrganizacion();
        System.out.println("DEBUG: Buscando PO para organización ID: " + organizacionId);


        Usuario po = usuarioRepository.findFirstByRol_IdRolAndOrganizacion_IdOrganizacion(1, organizacionId);

        System.out.println("DEBUG: PO encontrado: " + (po != null ? po.getNombre() + " (" + po.getCorreo() + ")" : "NINGUNO"));

        if (po != null) {
            String mensaje = String.format(
                    "Nueva solicitud de API pendiente de aprobación:\n" +
                            "• Desarrollador: %s (%s)\n" +
                            "• API solicitada: %s\n" +
                            "• Proyecto: %s\n" +
                            "• Descripción: %s\n" +
                            "• Entorno: %s\n\n" +
                            "Puedes aprobar o rechazar esta solicitud desde tu panel de administración.",
                    solicitud.getUsuario().getNombre() + " " + solicitud.getUsuario().getApellidoPaterno(),
                    solicitud.getUsuario().getCorreo(),
                    solicitud.getApi().getNombre(),
                    solicitud.getNombreProyecto(),
                    solicitud.getDescripcionUso(),
                    solicitud.getEntorno()
            );

            System.out.println("DEBUG: Enviando notificación al PO: " + po.getCorreo());
            enviarNotificacion(po, "Nueva Solicitud de API", mensaje);
            System.out.println("DEBUG: Notificación enviada correctamente");
        } else {
            System.err.println("ADVERTENCIA: No se encontró ningún PO para la organización ID: " + organizacionId);
        }
    }


    private SolicitudAccesoResponse mapearASolicitudAccesoResponse(SolicitudAcceso solicitud) {
        String estado = "PENDIENTE";
        if (solicitud.getEstado() != null && solicitud.getEstado()) {

            List<CredencialApi> credenciales = credencialApiRepository
                    .findByUsuario_DniAndApi_IdApiAndEstado(
                            solicitud.getUsuario().getDni(),
                            solicitud.getApi().getIdApi(),
                            true // estado activo
                    );
            estado = !credenciales.isEmpty() ? "APROBADA" : "RECHAZADA";
        }


        String fechaFormatted = "";
        if (solicitud.getFechaSolicitud() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            fechaFormatted = sdf.format(solicitud.getFechaSolicitud());
        }


        String desarrollador = "";
        String email = "";
        if (solicitud.getUsuario() != null) {
            String nombre = solicitud.getUsuario().getNombre() != null ? solicitud.getUsuario().getNombre() : "";
            String apellidoPaterno = solicitud.getUsuario().getApellidoPaterno() != null ? solicitud.getUsuario().getApellidoPaterno() : "";
            String apellidoMaterno = solicitud.getUsuario().getApellidoMaterno() != null ? solicitud.getUsuario().getApellidoMaterno() : "";

            desarrollador = (nombre + " " + apellidoPaterno + " " + apellidoMaterno).trim();
            email = solicitud.getUsuario().getCorreo() != null ? solicitud.getUsuario().getCorreo() : "";
        }

        return new SolicitudAccesoResponse(
                solicitud.getIdSolicitudAcceso(),
                solicitud.getApi().getNombre(),
                solicitud.getApi().getIdApi(),
                estado,
                solicitud.getFechaSolicitud(),
                solicitud.getFechaRespuesta(),
                solicitud.getNombreProyecto() != null ? solicitud.getNombreProyecto() : "",
                solicitud.getDescripcionUso() != null ? solicitud.getDescripcionUso() : "",
                desarrollador,
                email,
                fechaFormatted
        );
    }

    private CredencialApiResponse mapearACredencialApiResponse(CredencialApi credencial) {
        String estadoTexto = credencial.getEstado() ? "Activa" : "Inactiva";


        String entorno = apiHasEntornoRepository.findByApi_IdApi(credencial.getApi().getIdApi()).stream()
                .map(rel -> rel.getEntorno().getNombre())
                .filter(nombre -> !nombre.equalsIgnoreCase("Producción"))
                .findFirst()
                .orElse("sandbox");


        System.out.println("### MAPPER: Asignando entorno '" + entorno + "' a la credencial con API Key: " + credencial.getApiKey());

        return new CredencialApiResponse(
                credencial.getIdCredencialApi(),
                credencial.getApiKey(),
                credencial.getApi().getNombre(),
                credencial.getApi().getIdApi(),
                credencial.getFechaCreacion(),
                credencial.getEstado(),
                estadoTexto,
                entorno
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


    public List<SolicitudAccesoResponse> obtenerSolicitudesPorUsuario(String dniUsuario) {

        Usuario usuario = usuarioRepository.findByDni(dniUsuario);
        if (usuario == null) {
            throw new RuntimeException("Usuario no encontrado");
        }

        List<SolicitudAcceso> solicitudes = solicitudAccesoRepository.findByUsuario_DniOrderByFechaSolicitudDesc(dniUsuario);

        return solicitudes.stream()
                .map(this::mapearASolicitudAccesoResponse)
                .collect(Collectors.toList());
    }
}