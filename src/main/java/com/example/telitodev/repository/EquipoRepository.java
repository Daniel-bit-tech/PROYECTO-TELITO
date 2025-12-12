package com.example.telitodev.repository;

import com.example.telitodev.entity.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipoRepository extends JpaRepository<Equipo, Integer> {

    List<Equipo> findByOrganizacion_IdOrganizacion(Integer idOrganizacion);

    Optional<Equipo> findByNombre(String nombre);

    List<Equipo> findByNombreContainingIgnoreCase(String nombre);

    @Query("SELECT e FROM Equipo e JOIN e.usuarios u WHERE u.dni = :dniUsuario")
    List<Equipo> findByUsuarioDni(@Param("dniUsuario") String dniUsuario);

    @Query("SELECT e FROM Equipo e WHERE e.organizacion.idOrganizacion = :idOrganizacion " +
            "AND (e.nombre LIKE %:nombre% OR :nombre IS NULL)")
    List<Equipo> findByOrganizacionAndNombre(
            @Param("idOrganizacion") Integer idOrganizacion,
            @Param("nombre") String nombre);

    boolean existsByNombreAndOrganizacion_IdOrganizacion(String nombre, Integer idOrganizacion);
}