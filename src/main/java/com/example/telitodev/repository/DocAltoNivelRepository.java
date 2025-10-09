package com.example.telitodev.repository;


import com.example.telitodev.entity.doc_alto_nivel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocAltoNivelRepository extends JpaRepository<doc_alto_nivel, Integer> {

    // Método para obtener la documentación de alto nivel por idApi
    Optional<doc_alto_nivel> findByApi_IdApi(Integer idApi);
}
