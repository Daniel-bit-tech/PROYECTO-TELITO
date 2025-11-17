package com.example.telitodev.repository;

import com.example.telitodev.entity.Documentacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Repository
public interface DocumentacionRepository extends JpaRepository<Documentacion, Integer> {

    List<Documentacion> findByApi_IdApiOrderByFechaCreacionDesc(Integer id);
    Optional<Documentacion> findFirstByApi_IdApi(Integer apiId);

    Documentacion findByApi_IdApiAndFormato(Integer apiId, Documentacion.FormatoDoc formato);

}
