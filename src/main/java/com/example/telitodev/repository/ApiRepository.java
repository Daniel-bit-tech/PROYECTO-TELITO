package com.example.telitodev.repository;

import com.example.telitodev.entity.Api;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiRepository extends JpaRepository<Api, Integer> {

    Optional<Api> findById(Integer idApi);

    List<Api> findByDominio(String dominio);

    List<Api> findByTag(String tag);

    @Query(value = "SELECT a.* FROM api a " +
                    "WHERE (:search IS NULL OR LOWER(a.nombre) LIKE LOWER(CONCAT('%', :search, '%')))" +
                    "AND (:dominios IS NULL OR FIND_IN_SET(a.dominio, :dominios)) " +
                    "AND (:tags IS NULL OR FIND_IN_SET(a.tag, :tags))", nativeQuery = true)
    List<Api> findByFilters(@Param("search") String search,
                            @Param("dominios") String dominios,
                            @Param("tags") String tags);

}
