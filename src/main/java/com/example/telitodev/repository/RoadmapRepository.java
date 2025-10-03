package com.example.telitodev.repository;

import com.example.telitodev.entity.Roadmap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoadmapRepository extends JpaRepository<Roadmap, Integer> {
    // Aquí puedes agregar consultas personalizadas si es necesario
}
