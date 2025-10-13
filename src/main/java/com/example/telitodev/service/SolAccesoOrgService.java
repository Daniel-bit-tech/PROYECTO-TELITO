package com.example.telitodev.service;

import com.example.telitodev.entity.SolAccesoOrg;
import com.example.telitodev.entity.SolAccesoOrg.EstadoSolicitud;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.entity.Organizacion;
import com.example.telitodev.repository.SolAccesoOrgRepository;
import com.example.telitodev.repository.UsuarioRepository;
import com.example.telitodev.repository.OrganizacionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SolAccesoOrgService {

    private final SolAccesoOrgRepository solAccesoOrgRepository;
    private final UsuarioRepository usuarioRepository;
    private final OrganizacionRepository organizacionRepository;

    public SolAccesoOrgService(SolAccesoOrgRepository solAccesoOrgRepository,
                               UsuarioRepository usuarioRepository,
                               OrganizacionRepository organizacionRepository) {
        this.solAccesoOrgRepository = solAccesoOrgRepository;
        this.usuarioRepository = usuarioRepository;
        this.organizacionRepository = organizacionRepository;
    }

    // Crear nueva solicitud
    public SolAccesoOrg crearSolicitud(SolAccesoOrg solicitud, String dniUsuarioSolicitante) {
        // Validar que el usuario solicitante existe
        Usuario usuarioSolicitante = usuarioRepository.findByDni(dniUsuarioSolicitante);
        if (usuarioSolicitante == null) {
            throw new RuntimeException("Usuario solicitante no encontrado");
        }

        // Validar que la organización destino existe
        Optional<Organizacion> organizacionDestino = organizacionRepository.findById(solicitud.getOrganizacionDestino().getIdOrganizacion());
        if (organizacionDestino.isEmpty()) {
            throw new RuntimeException("Organización destino no encontrada");
        }

        // Validar que no existe solicitud pendiente para el mismo DNI
        if (solAccesoOrgRepository.existsByDniAndEstado(solicitud.getDni(), EstadoSolicitud.PENDIENTE)) {
            throw new RuntimeException("Ya existe una solicitud pendiente para este DNI");
        }

        // Validar que el DNI no esté ya registrado como usuario
        if (usuarioRepository.existsByDni(solicitud.getDni())) {
            throw new RuntimeException("El DNI ya está registrado en el sistema");
        }

        // Asignar usuario solicitante y organización destino
        solicitud.setUsuarioSolicitante(usuarioSolicitante);
        solicitud.setOrganizacionDestino(organizacionDestino.get());

        return solAccesoOrgRepository.save(solicitud);
    }

    // Obtener solicitud por ID
    public Optional<SolAccesoOrg> obtenerSolicitudPorId(Integer id) {
        return solAccesoOrgRepository.findById(id);
    }

    // Obtener todas las solicitudes (para admin)
    public List<SolAccesoOrg> obtenerTodasSolicitudes() {
        return solAccesoOrgRepository.findAll();
    }

    // Obtener solicitudes con paginación (para admin)
    public Page<SolAccesoOrg> obtenerSolicitudesPaginadas(Pageable pageable) {
        return solAccesoOrgRepository.findAll(pageable);
    }

    // Obtener solicitudes por estado
    public List<SolAccesoOrg> obtenerSolicitudesPorEstado(EstadoSolicitud estado) {
        return solAccesoOrgRepository.findByEstado(estado);
    }

    // Obtener solicitudes por estado con paginación
    public Page<SolAccesoOrg> obtenerSolicitudesPorEstado(EstadoSolicitud estado, Pageable pageable) {
        return solAccesoOrgRepository.findByEstado(estado, pageable);
    }

    // Obtener solicitudes pendientes
    public List<SolAccesoOrg> obtenerSolicitudesPendientes() {
        return solAccesoOrgRepository.findByEstado(EstadoSolicitud.PENDIENTE);
    }

    // Obtener solicitudes de un usuario específico
    public List<SolAccesoOrg> obtenerSolicitudesPorUsuario(String dniUsuario) {
        return solAccesoOrgRepository.findByUsuarioSolicitanteDni(dniUsuario);
    }

    // Obtener solicitudes pendientes por organización
    public List<SolAccesoOrg> obtenerSolicitudesPendientesPorOrganizacion(Integer idOrganizacion) {
        return solAccesoOrgRepository.findPendientesByOrganizacion(idOrganizacion);
    }

    // Aprobar solicitud
    public SolAccesoOrg aprobarSolicitud(Integer idSolicitud, String dniUsuarioRevisor, String comentarios) {
            Optional<SolAccesoOrg> solicitudOpt = solAccesoOrgRepository.findById(idSolicitud);
            if (solicitudOpt.isEmpty()) {
                throw new RuntimeException("Solicitud no encontrada");
            }

            SolAccesoOrg solicitud = solicitudOpt.get();
            if (!solicitud.isPendiente()) {
                throw new RuntimeException("La solicitud ya fue procesada");
            }

            Usuario usuarioRevisor = usuarioRepository.findByDni(dniUsuarioRevisor);
            if (usuarioRevisor == null) {
                throw new RuntimeException("Usuario revisor no encontrado");
            }

            // Actualizar estado y datos de revisión
            solicitud.setEstado(EstadoSolicitud.APROBADA);
            solicitud.setUsuarioRevisor(usuarioRevisor);
            solicitud.setFechaRevision(new Timestamp(System.currentTimeMillis()));
            solicitud.setComentariosRevisor(comentarios);

            return solAccesoOrgRepository.save(solicitud);
        }

        // Rechazar solicitud
        public SolAccesoOrg rechazarSolicitud(Integer idSolicitud, String dniUsuarioRevisor, String comentarios) {
            Optional<SolAccesoOrg> solicitudOpt = solAccesoOrgRepository.findById(idSolicitud);
            if (solicitudOpt.isEmpty()) {
                throw new RuntimeException("Solicitud no encontrada");
            }

            SolAccesoOrg solicitud = solicitudOpt.get();
            if (!solicitud.isPendiente()) {
                throw new RuntimeException("La solicitud ya fue procesada");
            }

            Usuario usuarioRevisor = usuarioRepository.findByDni(dniUsuarioRevisor);
            if (usuarioRevisor == null) {
                throw new RuntimeException("Usuario revisor no encontrado");
            }

            // Actualizar estado y datos de revisión
            solicitud.setEstado(EstadoSolicitud.RECHAZADA);
            solicitud.setUsuarioRevisor(usuarioRevisor);
            solicitud.setFechaRevision(new Timestamp(System.currentTimeMillis()));
            solicitud.setComentariosRevisor(comentarios);

            return solAccesoOrgRepository.save(solicitud);
        }

        // Verificar si existe solicitud pendiente para un DNI
        public boolean existeSolicitudPendienteParaDni(String dni) {
            return solAccesoOrgRepository.existsByDniAndEstado(dni, EstadoSolicitud.PENDIENTE);
        }

        // Obtener estadísticas de solicitudes
        public long contarSolicitudesPorEstado(EstadoSolicitud estado) {
            return solAccesoOrgRepository.countByEstado(estado);
        }
    /**
     * NUEVO: Método para crear solicitud CORREGIDO (para usuarios existentes)
     */
    /**
     * NUEVO: Método para crear solicitud CORREGIDO (para usuarios existentes)
     */
    /**
     * NUEVO: Método para crear solicitud CORREGIDO (para usuarios existentes)
     */
    public SolAccesoOrg crearSolicitudParaUsuarioExistente(SolAccesoOrg solicitud, String dniSolicitante) {
        // Validar que el usuario solicitante existe
        Usuario usuarioSolicitante = usuarioRepository.findByDni(dniSolicitante);
        if (usuarioSolicitante == null) {
            throw new RuntimeException("Usuario solicitante no encontrado");
        }

        // Validar que el usuario target existe (usando el DNI de la solicitud)
        String dniTarget = solicitud.getDni();
        Optional<Usuario> usuarioTargetOpt = usuarioRepository.findOptionalByDni(dniTarget);
        if (usuarioTargetOpt.isEmpty()) {
            throw new RuntimeException("Usuario con DNI " + dniTarget + " no encontrado en el sistema");
        }

        Usuario usuarioTarget = usuarioTargetOpt.get();

        // Validar que la organización destino existe
        Optional<Organizacion> organizacionDestino = organizacionRepository.findById(solicitud.getOrganizacionDestino().getIdOrganizacion());
        if (organizacionDestino.isEmpty()) {
            throw new RuntimeException("Organización destino no encontrada");
        }

        // ✅ VALIDACIÓN 1: Usuario NO tiene organización aprobada (usando método EXISTENTE)
        if (solAccesoOrgRepository.existsByDniAndEstado(dniTarget, EstadoSolicitud.APROBADA)) {
            throw new RuntimeException("El usuario " + usuarioTarget.getNombre() + " " + usuarioTarget.getApellidoPaterno() +
                    " ya pertenece a una organización. No puede ser agregado a otra.");
        }

        // ✅ VALIDACIÓN 2: No existe solicitud pendiente para este DNI (usando método EXISTENTE)
        if (solAccesoOrgRepository.existsByDniAndEstado(dniTarget, EstadoSolicitud.PENDIENTE)) {
            throw new RuntimeException("Ya existe una solicitud pendiente para el DNI: " + dniTarget);
        }

        // SOLO asignar usuario solicitante y organización destino
        solicitud.setUsuarioSolicitante(usuarioSolicitante);
        solicitud.setOrganizacionDestino(organizacionDestino.get());

        return solAccesoOrgRepository.save(solicitud);


    }

    public boolean existeSolicitudAprobadaParaDni(String dni) {
        return solAccesoOrgRepository.existsByDniAndEstado(dni, EstadoSolicitud.APROBADA);
    }

}
