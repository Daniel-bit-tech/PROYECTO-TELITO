package com.example.telitodev.repository;

import com.example.telitodev.entity.Organizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizacionRepository extends JpaRepository<Organizacion, Integer> {

        @Query("SELECT u.organizacion FROM Usuario u WHERE u.dni = :dniUsuario")
        Organizacion findByUsuarioDni(@Param("dniUsuario") String dniUsuario);

    // Nuevo metodo para buscar organización por ID para de la vista "Solicitudes de Acceso" del PO

        // Buscar organización por nombre (útil para formularios)
        Optional<Organizacion> findByNombre(String nombre);

        // Buscar todas las organizaciones activas (asumiendo que no hay campo estado)
        @Query("SELECT o FROM Organizacion o ORDER BY o.nombre")
        List<Organizacion> findAllOrderByNombre();

        // Verificar si existe organización por nombre
        boolean existsByNombre(String nombre);
        
        // Verificar si existe organización por dominio de correo
        boolean existsByDominioCorreo(String dominioCorreo);

        // Buscar organizaciones por IDs específicos
        List<Organizacion> findByIdOrganizacionIn(List<Integer> ids);

        // Contar cantidad de usuarios por organización
        @Query("SELECT o.nombre, COUNT(u) FROM Organizacion o LEFT JOIN o.usuarios u GROUP BY o.nombre")
        List<Object[]> countUsuariosPorOrganizacion();

}