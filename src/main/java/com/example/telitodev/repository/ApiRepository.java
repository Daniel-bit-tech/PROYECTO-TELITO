
package com.example.telitodev.repository;

import com.example.telitodev.dto.ApiProyectoDTO;
import com.example.telitodev.entity.Api;
import com.example.telitodev.entity.Equipo;
import com.example.telitodev.entity.Proyecto;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiRepository extends JpaRepository<Api, Integer> {

    Optional<Api> findById(Integer idApi);

    @Query("SELECT a FROM Api a " +
            "WHERE a.estadoApi.idEstado != 2 " +
            "AND a.idApi = :idApi")
    Optional<Api> findActiveApiById(@Param("idApi") Integer idApi);



    @Query("SELECT DISTINCT a FROM Api a " +
            "LEFT JOIN SolicitudAcceso s ON s.api = a AND s.usuario.dni = :dni " +
            "WHERE (a.equipo.idEquipo = :idEquipo) " +
            "OR (s.usuario.dni = :dni AND s.estado = true)")
    List<Api> findApisPermitidasParaSandbox(@Param("idEquipo") Integer idEquipo,
                                            @Param("dni") String dni);


    @Query(value = "SELECT DISTINCT a.* FROM api a " +
            "JOIN proyecto_has_api pha ON a.idAPI = pha.idAPI " +
            "JOIN proyecto p ON pha.idProyecto = p.idProyecto " +
            "WHERE p.idOrganizacion = :orgId " +
            "UNION " +

            "SELECT * FROM api WHERE idDominio = 11", nativeQuery = true)
    List<Api> findApisByOrgProjectsAndPublic(@Param("orgId") Integer idOrganizacion);

    // También necesitas el findByDominio_IdDominio si no lo tienes:
    List<Api> findByDominio_IdDominio(Integer idDominio);

    List<Api> findByTag_IdTag(Integer idTag);

    Api findByNombreIgnoreCase(String nombre);


    @Query("SELECT a FROM Api a " +
            "JOIN a.equipo eq " +
            "JOIN eq.organizacion o " +
            "LEFT JOIN a.dominio d " +
            "LEFT JOIN a.tag t " +
            "LEFT JOIN a.estadoApi e " +
            "WHERE o.idOrganizacion = :idOrganizacion " +
            "AND (:search IS NULL OR LOWER(a.nombre) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:idDominios IS NULL OR d.idDominio IN :idDominios) " +
            "AND (:idTags IS NULL OR t.idTag IN :idTags) " +
            "AND (:idEstados IS NULL OR e.idEstado IN :idEstados)")
    Page<Api> findByUserOrgAndFilters(@Param("search") String search,
                                      @Param("idDominios") List<Integer> idDominios,
                                      @Param("idTags") List<Integer> idTags,
                                      @Param("idEstados") List<Integer> idEstados,
                                      @Param("idOrganizacion") Integer idOrganizacion,
                                      Pageable pageable);
    @Query("SELECT a FROM Api a " +
            "LEFT JOIN a.dominio d " +
            "LEFT JOIN a.tag t " +
            "WHERE (:search IS NULL OR LOWER(a.nombre) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:idDominios IS NULL OR d.idDominio IN :idDominios) " +
            "AND (:idTags IS NULL OR t.idTag IN :idTags) " +
            "AND a.equipo.idEquipo = :idEquipoUsuario")
    List<Api> findByFilterAndDniUsuario(@Param("search") String search,
                                        @Param("idDominios") List<Integer> idDominios,
                                        @Param("idTags") List<Integer> idTags,
                                        @Param("idEquipoUsuario") Integer idEquipoUsuario);

    List<Api> findByNombreContainingIgnoreCase(String nombre);

    @Query(value = "SELECT a.* FROM api a " +
            "LEFT JOIN proyecto_has_api pha ON a.idAPI = pha.idAPI AND pha.idProyecto = :idProy " +
            "WHERE pha.idAPI IS NULL", nativeQuery = true)
    List<Api> findApisNotAssociatedWithProyecto(@Param("idProy") Integer idProyecto);

//    @Query("SELECT a FROM Api a " +
//            "LEFT JOIN a.dominio d " +
//            "LEFT JOIN a.tag t " +
//            "WHERE (:search IS NULL OR LOWER(a.nombre) LIKE LOWER(CONCAT('%', :search, '%'))) " +
//            "AND (:idDominios IS NULL OR d.idDominio IN :idDominios) " +
//            "AND (:idTags IS NULL OR t.idTag IN :idTags)")

    @Query(value = "SELECT a.* FROM api a " +
            "LEFT JOIN dominio d ON a.idDominio = d.idDominio " +
            "LEFT JOIN tag t ON a.idTag = t.idTag " +
            "WHERE (:search IS NULL OR LOWER(a.nombre) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:idDominios IS NULL OR d.idDominio IN (:idDominios)) " +
            "AND (:idTags IS NULL OR t.idTag IN (:idTags)) " +
            "AND a.idEstado!=2", nativeQuery = true)
    List<Api> findByFilters(@Param("search") String search,
                            @Param("idDominios") List<Integer> idDominios,
                            @Param("idTags") List<Integer> idTags);

    // Query simple para chatbot - solo datos básicos sin relaciones
    @Query(value = "SELECT idAPI, nombre, descripcion FROM api WHERE idEstado != 2", nativeQuery = true)
    List<Object[]> findBasicApiInfo();

    // Query nativo para obtener API por ID sin cargar relaciones LAZY
    @Query(value = "SELECT * FROM api WHERE idAPI = :idApi LIMIT 1", nativeQuery = true)
    Api findApiByIdNative(@Param("idApi") Integer idApi);


    /*
    @Query("SELECT new com.example.telitodev.dto.ApiProyectoDTO(" +
            "a.idApi, a.nombre, p.nombre, a.descripcion, a.endpointUrl, d.nombre, t.nombre, a.fechaCreacion) " +
            "FROM Api a " +
            "JOIN ProyectoHasApi pha ON pha.api = a " +
            "JOIN Proyecto p ON pha.proyecto = p " +
            "JOIN Dominio d ON a.dominio.idDominio = d.idDominio " +
            "JOIN Tag t ON a.tag.idTag = t.idTag " +
            "WHERE p.organizacion.idOrganizacion = (SELECT u.organizacion.idOrganizacion FROM Usuario u WHERE u.dni = :dni) " +
            "AND (:nombre IS NULL OR LOWER(a.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) " +
            "AND (:dominios IS NULL OR d.nombre IN :dominios) " +
            "AND (:tags IS NULL OR t.nombre IN :tags)")
    List<ApiProyectoDTO> findApisByFilters(@Param("dni") String dni,
                                           @Param("nombre") String nombre,
                                           @Param("dominios") List<String> dominios,
                                           @Param("tags") List<String> tags);

     */


    @Query("SELECT new com.example.telitodev.dto.ApiProyectoDTO(" +
            "a.idApi, a.nombre, eq.nombre, a.descripcion, a.endpointUrl, d.nombre, t.nombre, a.fechaCreacion, NULL) " +
            "FROM Api a " +
            "JOIN a.equipo eq " +
            "JOIN eq.usuarios u " +
            "JOIN a.apiHasEntornos ahe " +
            "JOIN ahe.entorno e " +
            "JOIN a.dominio d " +
            "JOIN a.tag t " +
            "WHERE u.dni = :dni " +
            "AND e.nombre = 'QA' " +
            "AND (:nombre IS NULL OR LOWER(a.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) " +
            "AND (:dominios IS NULL OR d.nombre IN :dominios) " +
            "AND (:tags IS NULL OR t.nombre IN :tags) " +
            "GROUP BY a.idApi, a.nombre, eq.nombre, a.descripcion, a.endpointUrl, d.nombre, t.nombre, a.fechaCreacion")
    Page<ApiProyectoDTO> findApisForQaCatalog(@Param("dni") String dni,
                                              @Param("nombre") String nombre,
                                              @Param("dominios") List<String> dominios,
                                              @Param("tags") List<String> tags,
                                              Pageable pageable);

    @Query("SELECT COUNT(DISTINCT a.idApi) " +
            "FROM Api a " +
            "JOIN a.equipo eq " +
            "JOIN eq.usuarios u " +
            "JOIN a.apiHasEntornos ahe " +
            "JOIN ahe.entorno e " +
            "WHERE u.dni = :dni " +
            "AND e.nombre = 'QA'")
    Integer countApisForQaValidation(@Param("dni") String dni);

    /**
     * Devuelve una lista completa (sin paginar) de las APIs que un QA necesita validar.
     * Basada en la estructura de equipos (sin PO Líder).
     */
    @Query("SELECT new com.example.telitodev.dto.ApiProyectoDTO(" +
            "a.idApi, a.nombre, eq.nombre, a.descripcion, a.endpointUrl, d.nombre, t.nombre, a.fechaCreacion, NULL) " +
            "FROM Api a " +
            "JOIN a.equipo eq " +
            "JOIN eq.usuarios u " +
            "JOIN a.apiHasEntornos ahe " +
            "JOIN ahe.entorno e " +
            "JOIN a.dominio d " +
            "JOIN a.tag t " +
            "WHERE u.dni = :dni " +
            "AND e.nombre = 'QA' " +
            "GROUP BY a.idApi, a.nombre, eq.nombre, a.descripcion, a.endpointUrl, d.nombre, t.nombre, a.fechaCreacion " +
            "ORDER BY a.nombre ASC")
    List<ApiProyectoDTO> findApisToReportForQa(@Param("dni") String dni);

    /**
     * Obtiene las últimas 5 APIs validadas (con reporte aprobado) para un usuario QA.
     */
    @Query("SELECT new com.example.telitodev.dto.ApiProyectoDTO(" +
            "a.idApi, a.nombre, eq.nombre, a.descripcion, a.endpointUrl, d.nombre, t.nombre, r.fechaCreacion, NULL) " +
            "FROM Api a " +
            "JOIN a.equipo eq " +
            "JOIN eq.usuarios u " +
            "JOIN Reporte r ON r.api = a " +
            "JOIN a.dominio d " +
            "JOIN a.tag t " +
            "WHERE u.dni = :dni " +
            "AND r.estado = 'Aprobado' " +
            "GROUP BY a.idApi, a.nombre, eq.nombre, a.descripcion, a.endpointUrl, d.nombre, t.nombre, r.fechaCreacion " +
            "ORDER BY r.fechaCreacion DESC")
    List<ApiProyectoDTO> findTop5ValidatedApisByQa(@Param("dni") String dni, Pageable pageable);


    /* ===== CONSULTAS ADICIONALES PARA ADMIN DASHBOARD ===== */
    // Cuenta APIs por entorno
    @Query(value = "SELECT COUNT(DISTINCT a.idAPI) FROM api a JOIN apihasentorno ahe ON a.idAPI = ahe.idAPI WHERE ahe.idEntorno = :idEntorno", nativeQuery = true)
    long countByEntorno(@Param("idEntorno") Integer idEntorno);

    //  Obtiene distribución de APIs por dominio

    @Query("SELECT d.nombre, COUNT(a) FROM Api a JOIN a.dominio d GROUP BY d.nombre ORDER BY COUNT(a) DESC")
    List<Object[]> getApiDistributionByDomain();

    // Obtiene distribución de APIs por tag

    @Query("SELECT t.nombre, COUNT(a) FROM Api a JOIN a.tag t GROUP BY t.nombre ORDER BY COUNT(a) DESC")
    List<Object[]> getApiDistributionByTag();

    // Obtiene APIs creadas en los últimos días

    @Query(value = "SELECT COUNT(*) FROM api WHERE fechaCreacion >= DATE_SUB(NOW(), INTERVAL :days DAY)", nativeQuery = true)
    long countApisCreatedInLastDays(@Param("days") int days);




    @Query("SELECT DISTINCT a FROM Api a " +
            "JOIN ProyectoHasApi pha ON pha.api = a " +
            "JOIN pha.proyecto p " +
            "WHERE p.equipo.organizacion.idOrganizacion = :idOrganizacion " +
            "AND a.estadoApi.idEstado != 2")
    List<Api> findApisDisponiblesPorOrganizacion(@Param("idOrganizacion") Integer idOrganizacion);

    @Query("SELECT a FROM Api a WHERE a.equipo.organizacion.idOrganizacion = :organizacionId")
    List<Api> findByOrganizacionId(@Param("organizacionId") Integer organizacionId);

    @Query("SELECT a FROM Api a WHERE a.equipo.idEquipo = :idEquipo")
    List<Api> findByEquipoId(@Param("idEquipo") Integer idEquipo);

    List<Api> findByEquipo_IdEquipo(Integer idEquipo);

    @Query("""
    select a
    from Api a
    """)
    List<Api> findApisToReportForQa();

    List<Api> findByEquipo(Equipo equipo);

    @Query("""
      select a
      from Api a
      where a.idApi not in (
          select pha.api.idApi
          from ProyectoHasApi pha
          where pha.proyecto.equipo.organizacion.idOrganizacion = :idOrg
      )
  """)
    List<Api> findApisDisponiblesParaOrganizacion(@Param("idOrg") Integer idOrg);


}
