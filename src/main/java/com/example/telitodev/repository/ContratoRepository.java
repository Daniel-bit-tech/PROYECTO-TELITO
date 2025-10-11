package com.example.telitodev.repository;

import com.example.telitodev.entity.ContratoApi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContratoRepository extends JpaRepository<ContratoApi, Integer> {

    List<ContratoApi> findByVersionApi_Api_IdApi(Integer idApi);

    ContratoApi findByIdContratoApiAndVersionApi_Api_IdApi(Integer idContrato, Integer idApi);

}
