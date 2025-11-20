package com.example.telitodev.repository;

import com.example.telitodev.entity.ContratoApi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContratoRepository extends JpaRepository<ContratoApi, Integer> {

    List<ContratoApi> findByVersionApi_Api_IdApi(Integer idApi);

    ContratoApi findByIdContratoApiAndVersionApi_Api_IdApi(Integer idContrato, Integer idApi);

    @Query("SELECT c FROM ContratoApi c " +
            "JOIN c.versionApi v " +
            "WHERE v.api.idApi = :apiId " +
            "ORDER BY v.fechaPublicacion DESC, v.idVersion DESC LIMIT 1")
    Optional<ContratoApi> findLatestByApiId(@Param("apiId") Integer apiId);

    ContratoApi findByVersionApi_IdVersion(Integer idVersion);
}
