package com.example.telitodev.repository;

import com.example.telitodev.entity.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProyectoRepository extends JpaRepository<Proyecto, Integer> {

    List<Proyecto> findByOrganizacion_Usuarios_Dni(@Param("dni") String dni);

    List<Proyecto> findByActivoAndOrganizacion_Usuarios_Dni(Boolean activo, String organizacion_usuarios_dni);

    List<Proyecto> findByPublicoAndOrganizacion_Usuarios_Dni(Boolean publico, String organizacion_usuarios_dni);
}
