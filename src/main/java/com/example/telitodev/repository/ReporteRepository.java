package com.example.telitodev.repository;

import com.example.telitodev.entity.Reporte;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.QueryHint;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Integer> {
    @Query("""
    SELECT r FROM Reporte r
    WHERE (:estados IS NULL OR r.estado IN :estados)
      AND (:inicio IS NULL OR r.fechaCreacion >= :inicio)
      AND (:fin IS NULL OR r.fechaCreacion <= :fin)
      AND (:nombreApi IS NULL OR LOWER(r.api.nombre) LIKE LOWER(CONCAT('%', :nombreApi, '%')))
""")
    List<Reporte> findByFilters(
            @Param("estados") List<String> estados,
            @Param("inicio") Timestamp inicio,
            @Param("fin") Timestamp fin,
            @Param("nombreApi") String nombreApi
    );

    // 🔹 Nuevo método con paginación
    @Query("""
    SELECT r FROM Reporte r
    WHERE (:estados IS NULL OR r.estado IN :estados)
      AND (:inicio IS NULL OR r.fechaCreacion >= :inicio)
      AND (:fin IS NULL OR r.fechaCreacion <= :fin)
      AND (:nombreApi IS NULL OR LOWER(r.api.nombre) LIKE LOWER(CONCAT('%', :nombreApi, '%')))
    """)
    Page<Reporte> findByFiltersPaged(
            @Param("estados") List<String> estados,
            @Param("inicio") Timestamp inicio,
            @Param("fin") Timestamp fin,
            @Param("nombreApi") String nombreApi,
            Pageable pageable
    );

}