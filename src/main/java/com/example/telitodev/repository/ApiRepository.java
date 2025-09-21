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

    List<Api> findByDominio_IdDominio(Integer idDominio);

    List<Api> findByTag_IdTag(Integer idTag);

    List<Api> findByNombreContainingIgnoreCase(String nombre);


    @Query(value = "SELECT a.* FROM api a " +
            "LEFT JOIN dominio d ON a.idDominio = d.idDominio " +
            "LEFT JOIN tag t ON a.idTag = t.idTag " +
            "WHERE (:search IS NULL OR LOWER(a.nombre) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:idDominios IS NULL OR d.idDominio IN (:idDominios)) " +
            "AND (:idTags IS NULL OR t.idTag IN (:idTags))", nativeQuery = true)
    List<Api> findByFilters(@Param("search") String search,
                            @Param("idDominios") List<Integer> idDominios,
                            @Param("idTags") List<Integer> idTags);
//                            @Param("idDominios") String idDominios,
//                            @Param("idTags") String idTags);


//    // El método de filtros múltiples que tenías
//    @Query(value = "SELECT a.* FROM api a " +
//            "WHERE (:search IS NULL OR LOWER(a.nombre) LIKE LOWER(CONCAT('%', :search, '%')))" +
//            "AND (:dominios IS NULL OR FIND_IN_SET(a.idDominio, :dominios)) " +
//            "AND (:tags IS NULL OR FIND_IN_SET(a.idTag, :tags))", nativeQuery = true)
//    List<Api> findByFilters(@Param("search") String search,
//                            @Param("dominios") String dominios,
//                            @Param("tags") String tags);

}
