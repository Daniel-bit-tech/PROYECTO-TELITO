package com.example.telitodev.repository;

import com.example.telitodev.entity.SolAccesoOrg;
import com.example.telitodev.entity.SolAccesoOrg.EstadoSolicitud;
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
public interface SolAccesoOrgRepository extends JpaRepository<SolAccesoOrg, Integer> {

    // Buscar solicitudes por estado
    List<SolAccesoOrg> findByEstado(EstadoSolicitud estado);

    // Buscar solicitudes por estado con paginación
    Page<SolAccesoOrg> findByEstado(EstadoSolicitud estado, Pageable pageable);

    // Buscar solicitudes por usuario solicitante
    List<SolAccesoOrg> findByUsuarioSolicitanteDni(String dniSolicitante);



    // Verificar si existe solicitud pendiente para un DNI específico
    boolean existsByDniAndEstado(String dni, EstadoSolicitud estado);

    // Buscar solicitud por DNI del solicitado
    Optional<SolAccesoOrg> findByDni(String dni);

    // Contar solicitudes por estado
    long countByEstado(EstadoSolicitud estado);

    // Búsqueda avanzada con múltiples filtros para administradores
    @Query("SELECT s FROM SolAccesoOrg s WHERE " +
            "(:estado IS NULL OR s.estado = :estado) AND " +
            "(:idOrganizacion IS NULL OR s.organizacionDestino.idOrganizacion = :idOrganizacion) AND " +
            "(:dniSolicitante IS NULL OR s.usuarioSolicitante.dni = :dniSolicitante)")
    Page<SolAccesoOrg> findByFiltrosAvanzados(
            @Param("estado") EstadoSolicitud estado,
            @Param("idOrganizacion") Integer idOrganizacion,
            @Param("dniSolicitante") String dniSolicitante,
            Pageable pageable);

    // Buscar solicitudes pendientes por organización destino
    @Query("SELECT s FROM SolAccesoOrg s WHERE s.estado = 'PENDIENTE' AND s.organizacionDestino.idOrganizacion = :idOrganizacion")
    List<SolAccesoOrg> findPendientesByOrganizacion(@Param("idOrganizacion") Integer idOrganizacion);

    // Obtener estadísticas de solicitudes por organización
    @Query("SELECT s.organizacionDestino.nombre, COUNT(s), s.estado " +
            "FROM SolAccesoOrg s " +
            "GROUP BY s.organizacionDestino.nombre, s.estado")
    List<Object[]> getEstadisticasPorOrganizacion();


}