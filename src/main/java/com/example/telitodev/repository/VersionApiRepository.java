package com.example.telitodev.repository;

import com.example.telitodev.entity.VersionApi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VersionApiRepository extends JpaRepository<VersionApi, Integer> {

    List<VersionApi> findByApi_IdApi(Integer id);

    Boolean existsByVersionAndApi_IdApi(String version, Integer idApi);

    VersionApi findByIdVersionAndApi_IdApi(Integer idVersion, Integer idApi);

    VersionApi findFirstByApi_IdApiOrderByIdVersionDesc(Integer idApi);

}
