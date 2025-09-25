package com.example.telitodev.repository;

import com.example.telitodev.entity.Entorno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntornoRepository extends JpaRepository<Entorno, Integer> {
}
