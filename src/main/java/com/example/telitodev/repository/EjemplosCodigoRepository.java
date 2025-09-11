package com.example.telitodev.repository;

import com.example.telitodev.entity.EjemplosCodigo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EjemplosCodigoRepository extends JpaRepository<EjemplosCodigo, Integer> {
}
