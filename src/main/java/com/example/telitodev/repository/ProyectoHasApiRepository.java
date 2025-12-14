package com.example.telitodev.repository;

import com.example.telitodev.entity.Api;
import com.example.telitodev.entity.Proyecto;
import com.example.telitodev.entity.ProyectoHasApi;
import com.example.telitodev.entity.ProyectoHasApiId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProyectoHasApiRepository extends JpaRepository<ProyectoHasApi, ProyectoHasApiId> {

    List<ProyectoHasApi> findByProyecto_IdProyecto(Integer id);

    ProyectoHasApi findByProyecto_IdProyectoAndApi_IdApi(Integer idProyecto, Integer idApi);

    @Query("SELECT DISTINCT pha.api FROM ProyectoHasApi pha WHERE pha.proyecto IN :proyectos")
    List<Api> findApisByProyectosIn(@Param("proyectos") List<Proyecto> proyectos);

    @Query("""
        SELECT DISTINCT pha.api
        FROM ProyectoHasApi pha
        JOIN pha.proyecto p
        JOIN p.equipo e
        JOIN e.organizacion o
        WHERE o.idOrganizacion = :orgId
    """)
    List<Api> findDistinctApisByOrganizacionId(@Param("orgId") Integer orgId);

    @Query("select pha.api.idApi from ProyectoHasApi pha where pha.proyecto.idProyecto = :idProyecto")
    List<Integer> findApiIdsByProyectoId(@Param("idProyecto") Integer idProyecto);



}

