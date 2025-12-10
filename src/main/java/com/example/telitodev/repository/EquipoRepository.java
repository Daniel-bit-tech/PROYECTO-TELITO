package com.example.telitodev.repository;

import com.example.telitodev.entity.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipoRepository extends JpaRepository<Equipo, Integer> {
    
    // Buscar por nombre
    Optional<Equipo> findByNombre(String nombre);
    
    // Buscar equipos por organización
    List<Equipo> findByOrganizacionIdOrganizacion(Integer idOrganizacion);
    
    // Verificar si existe equipo con el nombre
    boolean existsByNombre(String nombre);
}
