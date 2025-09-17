package com.example.telitodev.repository;

import com.example.telitodev.entity.Documentacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentacionRepository extends JpaRepository<Documentacion, Integer> {

    List<Documentacion> findByApi_IdApiOrderByFechaCreacionDesc(Integer id);
}
