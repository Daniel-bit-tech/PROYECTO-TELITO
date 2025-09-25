package com.example.telitodev.repository;

import com.example.telitodev.entity.Comentario;
import com.example.telitodev.entity.Evidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Integer> {

}