package com.example.telitodev.repository;

import com.example.telitodev.entity.Organizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizacionRepository extends JpaRepository<Organizacion, Integer> {

    @Query("SELECT u.organizacion FROM Usuario u WHERE u.dni = :dniUsuario")
    Organizacion findByUsuarioDni(@Param("dniUsuario") String dniUsuario);
}