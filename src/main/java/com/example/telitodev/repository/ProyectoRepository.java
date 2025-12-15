package com.example.telitodev.repository;

import com.example.telitodev.entity.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProyectoRepository extends JpaRepository<Proyecto, Integer> {

    // Proyectos donde el usuario pertenece a la organización del equipo
    List<Proyecto> findByEquipo_Organizacion_Usuarios_Dni(String dni);

    List<Proyecto> findByActivoAndEquipo_Organizacion_Usuarios_Dni(Boolean activo, String dni);

    List<Proyecto> findByPublicoAndEquipo_Organizacion_Usuarios_Dni(Boolean publico, String dni);

    // Proyectos por organización (vía Equipo -> Organizacion)
    List<Proyecto> findByEquipo_Organizacion_IdOrganizacion(Integer idOrganizacion);

    List<Proyecto> findByActivoAndEquipo_Organizacion_IdOrganizacion(Boolean activo, Integer idOrganizacion);

    List<Proyecto> findByPublicoAndEquipo_Organizacion_IdOrganizacion(Boolean publico, Integer idOrganizacion);

    // Filtros simples
    List<Proyecto> findByPublico(Boolean publico);
    List<Proyecto> findByActivo(Boolean activo);

    // Proyectos activos de una organización específica
    @Query("""
       SELECT DISTINCT p
       FROM Proyecto p
       JOIN p.equipo e
       JOIN e.organizacion o
       WHERE o.idOrganizacion = :organizacionId
         AND p.activo = true
       """)
    List<Proyecto> findProyectosActivosByOrganizacionId(@Param("organizacionId") Integer organizacionId);

    // Proyectos de una organización con sus APIs cargadas
    @Query("""
       SELECT DISTINCT p
       FROM Proyecto p
       JOIN p.equipo e
       JOIN e.organizacion o
       LEFT JOIN FETCH p.proyectoHasApis pha
       LEFT JOIN FETCH pha.api
       WHERE o.idOrganizacion = :organizacionId
       """)
    List<Proyecto> findByOrganizacionIdWithApis(@Param("organizacionId") Integer organizacionId);

    @Query("""
  select p
  from Proyecto p
  where p.equipo.organizacion.idOrganizacion = :orgId
    and (p.publico = true or p.equipo.idEquipo = :equipoId)
""")
    List<Proyecto> findVisiblesParaOrgYEquipo(@Param("orgId") Integer orgId,
                                              @Param("equipoId") Integer equipoId);

    @Query("""
  select p
  from Proyecto p
  where p.equipo.organizacion.idOrganizacion = :orgId
    and p.activo = true
    and (p.publico = true or p.equipo.idEquipo = :equipoId)
""")
    List<Proyecto> findVisiblesActivosParaOrgYEquipo(@Param("orgId") Integer orgId,
                                                     @Param("equipoId") Integer equipoId);

    // Para el tab "Ocultos/Privados": SOLO privados del equipo del PO
    List<Proyecto> findByPublicoFalseAndEquipo_IdEquipo(Integer idEquipo);


}
