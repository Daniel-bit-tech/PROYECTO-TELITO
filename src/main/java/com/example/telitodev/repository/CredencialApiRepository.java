package com.example.telitodev.repository;

import com.example.telitodev.entity.CredencialApi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CredencialApiRepository extends JpaRepository<CredencialApi, Integer> {

    List<CredencialApi> findByUsuario_Dni(String dni);
    Integer countByUsuario_DniAndEstado(String dni, Boolean Estado);
}
