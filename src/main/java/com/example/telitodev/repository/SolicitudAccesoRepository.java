package com.example.telitodev.repository;

import com.example.telitodev.entity.SolicitudAcceso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudAccesoRepository extends JpaRepository<SolicitudAcceso, Integer> {

    // Encontrar todas las solicitudes de un usuario
    List<SolicitudAcceso> findByUsuario_DniOrderByFechaSolicitudDesc(String dni);

    // Encontrar solicitudes por estado
    List<SolicitudAcceso> findByEstado(Boolean estado);

    // Encontrar solicitudes pendientes (false = pendiente, true = aprobada)
    @Query("SELECT s FROM SolicitudAcceso s WHERE s.estado = false ORDER BY s.fechaSolicitud DESC")
    List<SolicitudAcceso> findPendientesOrderByFechaSolicitudDesc();

    // Verificar si ya existe una solicitud pendiente para una API específica de un usuario
    @Query("SELECT s FROM SolicitudAcceso s WHERE s.usuario.dni = :dni AND s.api.idApi = :apiId AND s.estado = false")
    Optional<SolicitudAcceso> findSolicitudPendienteByUsuarioAndApi(@Param("dni") String dni, @Param("apiId") Integer apiId);

    // Encontrar solicitudes por usuario y API
    List<SolicitudAcceso> findByUsuario_DniAndApi_IdApi(String dni, Integer apiId);

    // Contar solicitudes pendientes de un usuario
    @Query("SELECT COUNT(s) FROM SolicitudAcceso s WHERE s.usuario.dni = :dni AND s.estado = false")
    Integer countSolicitudesPendientesByUsuario(@Param("dni") String dni);

    // Contar solicitudes aprobadas de un usuario
    Integer countByUsuario_DniAndEstado(String dni, Boolean estado);
}