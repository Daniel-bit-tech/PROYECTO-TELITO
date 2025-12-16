package com.example.telitodev.repository;

import com.example.telitodev.entity.CredencialApi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CredencialApiRepository extends JpaRepository<CredencialApi, Integer> {

    List<CredencialApi> findByUsuario_DniOrderByFechaCreacionDesc(String dni);
    Integer countByUsuario_DniAndEstado(String dni, Boolean Estado);
    List<CredencialApi> findByUsuario_Dni(String dni);

    List<CredencialApi> findByUsuario_DniAndApi_IdApiAndEstado(String dni, Integer apiId, Boolean estado);
    Optional<CredencialApi> findFirstByUsuario_DniAndApi_IdApiAndEstado(String userDni, Integer apiId, boolean estado);
    List<CredencialApi> findByApi_IdApiAndEstado(Integer idApi, Boolean estado);

    List<CredencialApi> findByUsuario_DniAndEstado(String dni, Boolean estado);


    Optional<CredencialApi> findByApiKeyAndUsuario_DniAndEstado(String apiKey, String dni, boolean estado);

}
