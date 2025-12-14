package com.example.telitodev.repository;

import com.example.telitodev.entity.Api;
import com.example.telitodev.entity.Equipo;
import com.example.telitodev.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface ApiRepository extends JpaRepository<Api, Integer> {

    // Métodos existentes (ajustar los que usan Usuario)
    List<Api> findByEquipo(Equipo equipo);


    //List<Api> findByEquipo_IdEquipo(Integer idEquipo);

    // Si tenías métodos que buscaban por usuario, ahora serán por equipo
    // Ejemplo: List<Api> findByUsuario(Usuario usuario); → List<Api> findByEquipo(Equipo equipo);

    // Mantener métodos de búsqueda por nombre (sin cambios)
    Api findByNombreIgnoreCase(String nombre);
    List<Api> findByNombreContainingIgnoreCase(String nombre);

    // Método para buscar APIs por estado (sin cambios)
    List<Api> findByEstadoApi_IdEstado(Integer idEstado);

    // Método para buscar APIs por dominio (sin cambios)
    List<Api> findByDominio_IdDominio(Integer idDominio);

    // Método para buscar APIs por tag (sin cambios)
    List<Api> findByTag_IdTag(Integer idTag);

    // Si tenías un método para buscar APIs de un usuario específico
    // Reemplazar por método que busque por equipo del usuario
    @Query("SELECT a FROM Api a JOIN a.equipo e JOIN e.usuarios u WHERE u.dni = :dniUsuario")
    List<Api> findByUsuarioDni(@Param("dniUsuario") String dniUsuario);

    // Método para buscar APIs con filtros (necesita ajuste si filtra por usuario)
    @Query("SELECT a FROM Api a WHERE " +
            "(:nombre IS NULL OR LOWER(a.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
            "(:idDominio IS NULL OR a.dominio.idDominio = :idDominio) AND " +
            "(:idTag IS NULL OR a.tag.idTag = :idTag) AND " +
            "(:idEstado IS NULL OR a.estadoApi.idEstado = :idEstado)")
    Page<Api> findWithFilters(
            @Param("nombre") String nombre,
            @Param("idDominio") Integer idDominio,
            @Param("idTag") Integer idTag,
            @Param("idEstado") Integer idEstado,
            Pageable pageable);

    // Si necesitas un método específico para el dashboard o estadísticas
    @Query("SELECT COUNT(a) FROM Api a WHERE a.equipo.idEquipo = :idEquipo")
    long countByEquipoId(@Param("idEquipo") Integer idEquipo);

    @Query("SELECT a FROM Api a WHERE a.fechaCreacion BETWEEN :fechaInicio AND :fechaFin")
    List<Api> findByFechaCreacionBetween(
            @Param("fechaInicio") Timestamp fechaInicio,
            @Param("fechaFin") Timestamp fechaFin);

    // Método para verificar si un usuario tiene acceso a una API específica
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
            "FROM Api a JOIN a.equipo e JOIN e.usuarios u " +
            "WHERE a.idApi = :idApi AND u.dni = :dniUsuario")
    boolean existsByIdApiAndUsuarioDni(
            @Param("idApi") Integer idApi,
            @Param("dniUsuario") String dniUsuario);



        @Query("SELECT COUNT(a) FROM Api a WHERE a.estadoApi.estado = 'Por Validar' " +
                "AND a.equipo.idEquipo NOT IN (SELECT u.equipo.idEquipo FROM Usuario u WHERE u.dni = :dniQa)")
        Integer countApisForQaValidation(@Param("dniQa") String dniQa);

    // Método para filtrar APIs (para ApiController)
    @Query("SELECT DISTINCT a FROM Api a " +
            "LEFT JOIN a.dominio d " +
            "LEFT JOIN a.tag t " +
            "WHERE (:nombre IS NULL OR LOWER(a.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) " +
            "AND (:dominios IS NULL OR d.idDominio IN :dominios) " +
            "AND (:tags IS NULL OR t.idTag IN :tags) " +
            "ORDER BY a.nombre")
    List<Api> findByFilters(@Param("nombre") String nombre,
                            @Param("dominios") List<Integer> dominios,
                            @Param("tags") List<Integer> tags);

    // Método para encontrar APIs no asociadas a un proyecto (para ProyectosPoController)
    @Query("SELECT a FROM Api a " +
            "WHERE a.idApi NOT IN (" +
            "   SELECT pa.api.idApi FROM ProyectoHasApi pa WHERE pa.proyecto.idProyecto = :proyectoId" +
            ") " +
            "AND a.estadoApi.estado = 'Activa' " + // Cambié 'Active' por 'Activa' (en español)
            "ORDER BY a.nombre")
    List<Api> findApiNotAssociatedWithProyecto(@Param("proyectoId") Integer proyectoId);

    // Método simplificado para QA - solo APIs por validar
    @Query("SELECT a FROM Api a " +
            "WHERE a.estadoApi.estado = 'Por Validar' " +
            "ORDER BY a.nombre")
    List<Api> findApisToReportForQa();


    // Método para encontrar APIs disponibles para una organización (para Onboarding)
    @Query("SELECT DISTINCT a FROM Api a " +
            "LEFT JOIN a.equipo e " +
            "WHERE a.estadoApi.estado = 'Activa' " +
            "AND (e IS NULL OR e.organizacion.idOrganizacion = :idOrganizacion) " +
            "ORDER BY a.nombre")
    List<Api> findApisDisponiblesParaOrganizacion(@Param("idOrganizacion") Integer idOrganizacion);

    List<Api> findByEquipo_IdEquipo(Integer idEquipo);

    List<Api> findByEquipo_IdEquipoAndIdApiNotIn(Integer idEquipo, List<Integer> ids);



        @Query("""
        SELECT a
        FROM Api a
        WHERE a.equipo.idEquipo = :idEquipo
          AND NOT EXISTS (
              SELECT 1
              FROM ProyectoHasApi pha
              WHERE pha.proyecto.idProyecto = :idProyecto
                AND pha.api.idApi = a.idApi
          )
    """)
        List<Api> findApisDisponiblesParaProyecto(@Param("idProyecto") Integer idProyecto,
                                                  @Param("idEquipo") Integer idEquipo);


}