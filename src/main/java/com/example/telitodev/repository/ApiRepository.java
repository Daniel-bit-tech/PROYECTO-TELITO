
package com.example.telitodev.repository;

import com.example.telitodev.dto.ApiProyectoDTO;
import com.example.telitodev.entity.Api;
import com.example.telitodev.entity.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiRepository extends JpaRepository<Api, Integer> {

    Optional<Api> findById(Integer idApi);

    List<Api> findByDominio_IdDominio(Integer idDominio);

    List<Api> findByTag_IdTag(Integer idTag);

    List<Api> findByNombreContainingIgnoreCase(String nombre);

    @Query(value = "SELECT a.* FROM api a " +
            "LEFT JOIN proyecto_has_api pha ON a.idAPI = pha.idAPI AND pha.idProyecto = :idProy " +
            "WHERE pha.idAPI IS NULL", nativeQuery = true)
    List<Api> findApisNotAssociatedWithProyecto(@Param("idProy") Integer idProyecto);

    @Query(value = "SELECT a.* FROM api a " +
            "LEFT JOIN dominio d ON a.idDominio = d.idDominio " +
            "LEFT JOIN tag t ON a.idTag = t.idTag " +
            "WHERE (:search IS NULL OR LOWER(a.nombre) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:idDominios IS NULL OR d.idDominio IN (:idDominios)) " +
            "AND (:idTags IS NULL OR t.idTag IN (:idTags))", nativeQuery = true)
    List<Api> findByFilters(@Param("search") String search,
                            @Param("idDominios") List<Integer> idDominios,
                            @Param("idTags") List<Integer> idTags);

    @Query("SELECT new com.example.telitodev.dto.ApiProyectoDTO(a.nombre, p.nombre, a.descripcion, " +
            "a.endpointUrl, d.nombre, t.nombre, a.fechaCreacion) " +
            "FROM Api a " +
            "JOIN ProyectoHasApi pha ON pha.api = a " +
            "JOIN Proyecto p ON pha.proyecto = p " +
            "JOIN Dominio d ON a.dominio.idDominio = d.idDominio " +  // Usamos a.dominio.idDominio
            "JOIN Tag t ON a.tag.idTag = t.idTag " +  // Usamos a.tag.idTag
            "WHERE p.organizacion.idOrganizacion = (SELECT u.organizacion.idOrganizacion FROM Usuario u WHERE u.dni = :dni)")
    List<ApiProyectoDTO> findApisByUsuarioAndProyecto(@Param("dni") String dni);

    @Query("SELECT new com.example.telitodev.dto.ApiProyectoDTO(a.nombre, p.nombre, a.descripcion, " +
            "a.endpointUrl, d.nombre, t.nombre, a.fechaCreacion) " +
            "FROM Api a " +
            "JOIN ProyectoHasApi pha ON pha.api = a " +
            "JOIN Proyecto p ON pha.proyecto = p " +
            "JOIN Dominio d ON a.dominio.idDominio = d.idDominio " +
            "JOIN Tag t ON a.tag.idTag = t.idTag " +
            "WHERE p.organizacion.idOrganizacion = (SELECT u.organizacion.idOrganizacion FROM Usuario u WHERE u.dni = :dni) " +
            "AND LOWER(a.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<ApiProyectoDTO> findApisByUsuarioAndProyectoAndNombre(@Param("dni") String dni,
                                                               @Param("nombre") String nombre);

    @Query("SELECT new com.example.telitodev.dto.ApiProyectoDTO(" +
            "a.nombre, p.nombre, a.descripcion, a.endpointUrl, d.nombre, t.nombre, a.fechaCreacion) " +
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
}
