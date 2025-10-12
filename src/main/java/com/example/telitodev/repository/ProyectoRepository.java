package com.example.telitodev.repository;

import com.example.telitodev.entity.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProyectoRepository extends JpaRepository<Proyecto, Integer> {

    List<Proyecto> findByOrganizacion_Usuarios_Dni(@Param("dni") String dni);

    List<Proyecto> findByActivoAndOrganizacion_Usuarios_Dni(Boolean activo, String organizacion_usuarios_dni);

    List<Proyecto> findByPublicoAndOrganizacion_Usuarios_Dni(Boolean publico, String organizacion_usuarios_dni);

    // Consultas por organización (más apropiadas)
    List<Proyecto> findByOrganizacion_IdOrganizacion(Integer idOrganizacion);
    
    List<Proyecto> findByActivoAndOrganizacion_IdOrganizacion(Boolean activo, Integer idOrganizacion);
    
    List<Proyecto> findByPublicoAndOrganizacion_IdOrganizacion(Boolean publico, Integer idOrganizacion);

    List<Proyecto> findByPublico(Boolean publico);
    List<Proyecto> findByActivo(Boolean activo);


    // Para obtener proyectos activos de una organización específica
    @Query("SELECT p FROM Proyecto p WHERE p.organizacion.idOrganizacion = :organizacionId AND p.activo = true")
    List<Proyecto> findProyectosActivosByOrganizacionId(@Param("organizacionId") Integer organizacionId);

    // Para obtener proyectos con sus APIs cargadas
    @Query("SELECT p FROM Proyecto p LEFT JOIN FETCH p.proyectoHasApis pha LEFT JOIN FETCH pha.api WHERE p.organizacion.idOrganizacion = :organizacionId")
    List<Proyecto> findByOrganizacionIdWithApis(@Param("organizacionId") Integer organizacionId);

}
