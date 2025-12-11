package com.example.telitodev.repository;

import com.example.telitodev.entity.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProyectoRepository extends JpaRepository<Proyecto, Integer> {

    // TODO: Proyecto now belongs to Equipo, not Organizacion directly
    // Projects are accessed through equipo which belongs to organizacion
    @Query("SELECT p FROM Proyecto p WHERE p.equipo.idEquipo IN (SELECT u.equipo.idEquipo FROM Usuario u WHERE u.dni = :dni)")
    List<Proyecto> findByEquipoUsuarioDni(@Param("dni") String dni);

    @Query("SELECT p FROM Proyecto p WHERE p.activo = :activo AND p.equipo.idEquipo IN (SELECT u.equipo.idEquipo FROM Usuario u WHERE u.dni = :dni)")
    List<Proyecto> findByActivoAndEquipoUsuarioDni(@Param("activo") Boolean activo, @Param("dni") String dni);

    @Query("SELECT p FROM Proyecto p WHERE p.publico = :publico AND p.equipo.idEquipo IN (SELECT u.equipo.idEquipo FROM Usuario u WHERE u.dni = :dni)")
    List<Proyecto> findByPublicoAndEquipoUsuarioDni(@Param("publico") Boolean publico, @Param("dni") String dni);

    // Consultas por equipo
    List<Proyecto> findByEquipo_IdEquipo(Integer idEquipo);
    
    List<Proyecto> findByActivoAndEquipo_IdEquipo(Boolean activo, Integer idEquipo);
    
    List<Proyecto> findByPublicoAndEquipo_IdEquipo(Boolean publico, Integer idEquipo);
    
    // Consultas por organización (a través de equipo)
    @Query("SELECT p FROM Proyecto p WHERE p.equipo.organizacion.idOrganizacion = :idOrganizacion")
    List<Proyecto> findByEquipoOrganizacionId(@Param("idOrganizacion") Integer idOrganizacion);
    
    @Query("SELECT p FROM Proyecto p WHERE p.activo = :activo AND p.equipo.organizacion.idOrganizacion = :idOrganizacion")
    List<Proyecto> findByActivoAndEquipoOrganizacionId(@Param("activo") Boolean activo, @Param("idOrganizacion") Integer idOrganizacion);
    
    @Query("SELECT p FROM Proyecto p WHERE p.publico = :publico AND p.equipo.organizacion.idOrganizacion = :idOrganizacion")
    List<Proyecto> findByPublicoAndEquipoOrganizacionId(@Param("publico") Boolean publico, @Param("idOrganizacion") Integer idOrganizacion);

    List<Proyecto> findByPublico(Boolean publico);
    List<Proyecto> findByActivo(Boolean activo);


    // Para obtener proyectos activos de una organización específica (a través de equipo)
    @Query("SELECT p FROM Proyecto p WHERE p.equipo.organizacion.idOrganizacion = :organizacionId AND p.activo = true")
    List<Proyecto> findProyectosActivosByOrganizacionId(@Param("organizacionId") Integer organizacionId);

    // Para obtener proyectos con sus APIs cargadas (a través de equipo)
    @Query("SELECT p FROM Proyecto p LEFT JOIN FETCH p.proyectoHasApis pha LEFT JOIN FETCH pha.api WHERE p.equipo.organizacion.idOrganizacion = :organizacionId")
    List<Proyecto> findByOrganizacionIdWithApis(@Param("organizacionId") Integer organizacionId);

}
