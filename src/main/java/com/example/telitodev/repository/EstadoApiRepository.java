package com.example.telitodev.repository;

import com.example.telitodev.entity.EstadoApi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstadoApiRepository extends JpaRepository<EstadoApi, Integer> {

    EstadoApi getByEstado(String estadoApi);

}
