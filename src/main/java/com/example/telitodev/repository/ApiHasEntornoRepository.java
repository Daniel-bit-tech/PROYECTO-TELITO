package com.example.telitodev.repository;

import com.example.telitodev.entity.ApiHasEntorno;
import com.example.telitodev.entity.ApiHasEntornoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiHasEntornoRepository extends JpaRepository<ApiHasEntorno, ApiHasEntornoId> {


    List<ApiHasEntorno> findByApi_IdApi(Integer apiId);
    Optional<ApiHasEntorno> findByApi_IdApiAndEntorno_IdEntorno(Integer apiId, Integer entornoId);
    Optional<ApiHasEntorno> findFirstByApi_IdApiAndEstado(Integer apiId, ApiHasEntorno.EstadoApiEntorno estado);
    List<ApiHasEntorno> findByApi_IdApiAndEstado(Integer apiId, ApiHasEntorno.EstadoApiEntorno estado);

}