package com.example.telitodev.repository;

import com.example.telitodev.entity.SolAccesoEquipo;
import com.example.telitodev.entity.SolAccesoEquipo.EstadoSolicitud;
import com.example.telitodev.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolAccesoEquipoRepository extends JpaRepository<SolAccesoEquipo, Integer> {

    // Buscar solicitudes por estado
    List<SolAccesoEquipo> findByEstado(EstadoSolicitud estado);

    // Buscar solicitudes por estado con paginación
    Page<SolAccesoEquipo> findByEstado(EstadoSolicitud estado, Pageable pageable);

    // Buscar solicitudes por usuario solicitante
    List<SolAccesoEquipo> findByUsuarioSolicitanteDni(String dniSolicitante);



    // Verificar si existe solicitud pendiente para un DNI específico
    boolean existsByDniAndEstado(String dni, EstadoSolicitud estado);

    // Buscar solicitud por DNI del solicitado
    Optional<SolAccesoEquipo> findByDni(String dni);

    // Contar solicitudes por estado
    long countByEstado(EstadoSolicitud estado);

    // Búsqueda avanzada con múltiples filtros para administradores
    @Query("SELECT s FROM SolAccesoEquipo s WHERE " +
            "(:estado IS NULL OR s.estado = :estado) AND " +
            "(:idEquipo IS NULL OR s.equipoDestino.idEquipo = :idEquipo) AND " +
            "(:dniSolicitante IS NULL OR s.usuarioSolicitante.dni = :dniSolicitante)")
    Page<SolAccesoEquipo> findByFiltrosAvanzados(
            @Param("estado") EstadoSolicitud estado,
            @Param("idEquipo") Integer idEquipo,
            @Param("dniSolicitante") String dniSolicitante,
            Pageable pageable);

    // Buscar solicitudes pendientes por equipo destino
    @Query("SELECT s FROM SolAccesoEquipo s WHERE s.estado = 'PENDIENTE' AND s.equipoDestino.idEquipo = :idEquipo")
    List<SolAccesoEquipo> findPendientesByEquipo(@Param("idEquipo") Integer idEquipo);

    // Obtener estadísticas de solicitudes por equipo
    @Query("SELECT s.equipoDestino.nombre, COUNT(s), s.estado " +
            "FROM SolAccesoEquipo s " +
            "GROUP BY s.equipoDestino.nombre, s.estado")
    List<Object[]> getEstadisticasPorEquipo();

    // Este query es para el historial de solicitudes

    // Buscar todas las solicitudes realizadas por un usuario solicitante específico (POR EL PO)
    @Query("SELECT s FROM SolAccesoEquipo s WHERE s.usuarioSolicitante.dni = :dniSolicitante ORDER BY s.fechaSolicitud DESC")
    List<SolAccesoEquipo> findByUsuarioSolicitanteDniOrderByFechaDesc(@Param("dniSolicitante") String dniSolicitante);

}