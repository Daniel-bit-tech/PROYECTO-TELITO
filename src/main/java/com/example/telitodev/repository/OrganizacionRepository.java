package com.example.telitodev.repository;

import com.example.telitodev.entity.Organizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizacionRepository extends JpaRepository<Organizacion, Integer> {
    
    /**
     * Encuentra organización por nombre
     */
    Organizacion findByNombre(String nombre);
}
