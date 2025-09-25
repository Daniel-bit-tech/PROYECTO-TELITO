package com.example.telitodev.repository;

import com.example.telitodev.entity.ProyectoHasApi;
import com.example.telitodev.entity.ProyectoHasApiId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProyectoHasApiRepository extends JpaRepository<ProyectoHasApi, ProyectoHasApiId> {

    List<ProyectoHasApi> findByProyecto_IdProyecto(Integer id);
}
