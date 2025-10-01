package com.example.telitodev.repository;

import com.example.telitodev.entity.Backlog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BacklogRepository extends JpaRepository<Backlog, Integer> {
    // Aquí podrías agregar otros métodos personalizados si es necesario
}
