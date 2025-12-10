//package com.example.telitodev.service;
//
//import com.example.telitodev.entity.SolAccesoEquipo;
//import com.example.telitodev.entity.SolAccesoEquipo.EstadoSolicitud;
//import com.example.telitodev.entity.Usuario;
//import com.example.telitodev.entity.Equipo;
//import com.example.telitodev.repository.SolAccesoOrgRepository;
//import com.example.telitodev.repository.UsuarioRepository;
//import com.example.telitodev.repository.EquipoRepository;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.sql.Timestamp;
//import java.util.List;
//import java.util.Optional;
//
//@Service
//@Transactional
//public class SolAccesoOrgService {
//
//    private final SolAccesoOrgRepository solAccesoOrgRepository;
//    private final UsuarioRepository usuarioRepository;
//    private final EquipoRepository equipoRepository;
//
//    public SolAccesoOrgService(SolAccesoOrgRepository solAccesoOrgRepository,
//                               UsuarioRepository usuarioRepository,
//                               EquipoRepository equipoRepository) {
//        this.solAccesoOrgRepository = solAccesoOrgRepository;
//        this.usuarioRepository = usuarioRepository;
//        this.equipoRepository = equipoRepository;
//    }
//
//    // Crear nueva solicitud
//    public SolAccesoOrg crearSolicitud(SolAccesoOrg solicitud, String dniUsuarioSolicitante) {
//        // Validar que el usuario solicitante existe
//        Usuario usuarioSolicitante = usuarioRepository.findByDni(dniUsuarioSolicitante);
//        if (usuarioSolicitante == null) {
//            throw new RuntimeException("Usuario solicitante no encontrado");
//        }
//
//        // Validar que el equipo destino existe
//        Optional<Equipo> equipoDestino = equipoRepository.findById(solicitud.getEquipoDestino().getIdEquipo());
//        if (equipoDestino.isEmpty()) {
//            throw new RuntimeException("Equipo destino no encontrado");
//        }
//
//        // Validar que no existe solicitud pendiente para el mismo DNI
//        if (solAccesoOrgRepository.existsByDniAndEstado(solicitud.getDni(), EstadoSolicitud.PENDIENTE)) {
//            throw new RuntimeException("Ya existe una solicitud pendiente para este DNI");
//        }
//
//        // Validar que el DNI no esté ya registrado como usuario
//        if (usuarioRepository.existsByDni(solicitud.getDni())) {
//            throw new RuntimeException("El DNI ya está registrado en el sistema");
//        }
//
//        // Asignar usuario solicitante y equipo destino
//        solicitud.setUsuarioSolicitante(usuarioSolicitante);
//        solicitud.setEquipoDestino(equipoDestino.get());
//
//        return solAccesoOrgRepository.save(solicitud);
//    }
//
//    // Obtener solicitud por ID
//    public Optional<SolAccesoOrg> obtenerSolicitudPorId(Integer id) {
//        return solAccesoOrgRepository.findById(id);
//    }
//
//    // Obtener todas las solicitudes (para admin)
//    public List<SolAccesoOrg> obtenerTodasSolicitudes() {
//        return solAccesoOrgRepository.findAll();
//    }
//
//    // Obtener solicitudes con paginación (para admin)
//    public Page<SolAccesoOrg> obtenerSolicitudesPaginadas(Pageable pageable) {
//        return solAccesoOrgRepository.findAll(pageable);
//    }
//
//    // Obtener solicitudes por estado
//    public List<SolAccesoOrg> obtenerSolicitudesPorEstado(EstadoSolicitud estado) {
//        return solAccesoOrgRepository.findByEstado(estado);
//    }
//
//    // Obtener solicitudes por estado con paginación
//    public Page<SolAccesoOrg> obtenerSolicitudesPorEstado(EstadoSolicitud estado, Pageable pageable) {
//        return solAccesoOrgRepository.findByEstado(estado, pageable);
//    }
//
//    // Obtener solicitudes pendientes
//    public List<SolAccesoOrg> obtenerSolicitudesPendientes() {
//        return solAccesoOrgRepository.findByEstado(EstadoSolicitud.PENDIENTE);
//    }
//
//    // Obtener solicitudes de un usuario específico
//    public List<SolAccesoOrg> obtenerSolicitudesPorUsuario(String dniUsuario) {
//        return solAccesoOrgRepository.findByUsuarioSolicitanteDni(dniUsuario);
//    }
//
//    // Obtener solicitudes pendientes por equipo
//    public List<SolAccesoOrg> obtenerSolicitudesPendientesPorEquipo(Integer idEquipo) {
//        return solAccesoOrgRepository.findPendientesByEquipo(idEquipo);
//    }
//
//    // Aprobar solicitud
//    public SolAccesoOrg aprobarSolicitud(Integer idSolicitud, String dniUsuarioRevisor, String comentarios) {
//            Optional<SolAccesoOrg> solicitudOpt = solAccesoOrgRepository.findById(idSolicitud);
//            if (solicitudOpt.isEmpty()) {
//                throw new RuntimeException("Solicitud no encontrada");
//            }
//
//            SolAccesoOrg solicitud = solicitudOpt.get();
//            if (!solicitud.isPendiente()) {
//                throw new RuntimeException("La solicitud ya fue procesada");
//            }
//
//            Usuario usuarioRevisor = usuarioRepository.findByDni(dniUsuarioRevisor);
//            if (usuarioRevisor == null) {
//                throw new RuntimeException("Usuario revisor no encontrado");
//            }
//
//            // ✅ ACTUALIZAR EQUIPO DEL USUARIO OBJETIVO
//            Usuario usuarioObjetivo = usuarioRepository.findByDni(solicitud.getDni());
//            if (usuarioObjetivo != null) {
//                usuarioObjetivo.setEquipo(solicitud.getEquipoDestino());
//                usuarioRepository.save(usuarioObjetivo);
//                System.out.println("✅ Usuario " + usuarioObjetivo.getNombre() + " asignado a equipo: " +
//                    solicitud.getEquipoDestino().getNombre());
//            } else {
//                System.out.println("⚠️ Usuario con DNI " + solicitud.getDni() + " no encontrado para asignar equipo");
//            }
//
//            // Actualizar estado y datos de revisión
//            solicitud.setEstado(EstadoSolicitud.APROBADA);
//            solicitud.setUsuarioRevisor(usuarioRevisor);
//            solicitud.setFechaRevision(new Timestamp(System.currentTimeMillis()));
//            solicitud.setComentariosRevisor(comentarios);
//
//            return solAccesoOrgRepository.save(solicitud);
//        }
//
//        // Rechazar solicitud
//        public SolAccesoOrg rechazarSolicitud(Integer idSolicitud, String dniUsuarioRevisor, String comentarios) {
//            Optional<SolAccesoOrg> solicitudOpt = solAccesoOrgRepository.findById(idSolicitud);
//            if (solicitudOpt.isEmpty()) {
//                throw new RuntimeException("Solicitud no encontrada");
//            }
//
//            SolAccesoOrg solicitud = solicitudOpt.get();
//            if (!solicitud.isPendiente()) {
//                throw new RuntimeException("La solicitud ya fue procesada");
//            }
//
//            Usuario usuarioRevisor = usuarioRepository.findByDni(dniUsuarioRevisor);
//            if (usuarioRevisor == null) {
//                throw new RuntimeException("Usuario revisor no encontrado");
//            }
//
//            // Actualizar estado y datos de revisión
//            solicitud.setEstado(EstadoSolicitud.RECHAZADA);
//            solicitud.setUsuarioRevisor(usuarioRevisor);
//            solicitud.setFechaRevision(new Timestamp(System.currentTimeMillis()));
//            solicitud.setComentariosRevisor(comentarios);
//
//            return solAccesoOrgRepository.save(solicitud);
//        }
//
//        // Verificar si existe solicitud pendiente para un DNI
//        public boolean existeSolicitudPendienteParaDni(String dni) {
//            return solAccesoOrgRepository.existsByDniAndEstado(dni, EstadoSolicitud.PENDIENTE);
//        }
//
//        // Obtener estadísticas de solicitudes
//        public long contarSolicitudesPorEstado(EstadoSolicitud estado) {
//            return solAccesoOrgRepository.countByEstado(estado);
//        }
//    /**
//     * NUEVO: Método para crear solicitud CORREGIDO (para usuarios existentes)
//     */
//    /**
//     * NUEVO: Método para crear solicitud CORREGIDO (para usuarios existentes)
//     */
//    /**
//     * NUEVO: Método para crear solicitud CORREGIDO (para usuarios existentes)
//     */
//    public SolAccesoOrg crearSolicitudParaUsuarioExistente(SolAccesoOrg solicitud, String dniSolicitante) {
//        // Validar que el usuario solicitante existe
//        Usuario usuarioSolicitante = usuarioRepository.findByDni(dniSolicitante);
//        if (usuarioSolicitante == null) {
//            throw new RuntimeException("Usuario solicitante no encontrado");
//        }
//
//        // Validar que el usuario target existe (usando el DNI de la solicitud)
//        String dniTarget = solicitud.getDni();
//        Optional<Usuario> usuarioTargetOpt = usuarioRepository.findOptionalByDni(dniTarget);
//        if (usuarioTargetOpt.isEmpty()) {
//            throw new RuntimeException("Usuario con DNI " + dniTarget + " no encontrado en el sistema");
//        }
//
//        Usuario usuarioTarget = usuarioTargetOpt.get();
//
//        // Validar que el equipo destino existe
//        Optional<Equipo> equipoDestino = equipoRepository.findById(solicitud.getEquipoDestino().getIdEquipo());
//        if (equipoDestino.isEmpty()) {
//            throw new RuntimeException("Equipo destino no encontrado");
//        }
//
//        // ✅ VALIDACIÓN 1: Usuario NO tiene equipo aprobado (usando método EXISTENTE)
//        if (solAccesoOrgRepository.existsByDniAndEstado(dniTarget, EstadoSolicitud.APROBADA)) {
//            throw new RuntimeException("El usuario " + usuarioTarget.getNombre() + " " + usuarioTarget.getApellidoPaterno() +
//                    " ya pertenece a un equipo. No puede ser agregado a otro.");
//        }
//
//        // ✅ VALIDACIÓN 2: No existe solicitud pendiente para este DNI (usando método EXISTENTE)
//        if (solAccesoOrgRepository.existsByDniAndEstado(dniTarget, EstadoSolicitud.PENDIENTE)) {
//            throw new RuntimeException("Ya existe una solicitud pendiente para el DNI: " + dniTarget);
//        }
//
//        // SOLO asignar usuario solicitante y equipo destino
//        solicitud.setUsuarioSolicitante(usuarioSolicitante);
//        solicitud.setEquipoDestino(equipoDestino.get());
//
//        return solAccesoOrgRepository.save(solicitud);
//
//
//    }
//
//    public boolean existeSolicitudAprobadaParaDni(String dni) {
//        return solAccesoOrgRepository.existsByDniAndEstado(dni, EstadoSolicitud.APROBADA);
//    }
//
//}
