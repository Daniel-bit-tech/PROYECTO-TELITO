package com.example.telitodev.repository;

import com.example.telitodev.entity.Issue;
import com.example.telitodev.entity.IssueId;
import com.example.telitodev.entity.Reporte;
import com.example.telitodev.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface IssueRepository extends JpaRepository<Issue, IssueId> {
    @Query("SELECT i FROM Issue i " +
            "WHERE (:estados IS NULL OR i.estado IN :estados) " +
            "AND (:inicio IS NULL OR i.fechaCreacion >= :inicio) " +
            "AND (:fin IS NULL OR i.fechaCreacion <= :fin) " +
            "AND (:nombre IS NULL OR LOWER(i.descripcion) LIKE LOWER(CONCAT('%', :nombre, '%')))")
    Page<Issue> findByFilters(@Param("estados") List<String> estados,
                              @Param("inicio") Timestamp inicio,
                              @Param("fin") Timestamp fin,
                              @Param("nombre") String nombre,
                              Pageable pageable);

    @Query("""
        SELECT i 
        FROM Issue i 
        WHERE (:estados IS NULL OR i.estado IN :estados)
          AND (:inicio IS NULL OR i.fechaCreacion >= :inicio)
          AND (:fin IS NULL OR i.fechaCreacion <= :fin)
          AND (:nombre IS NULL OR LOWER(i.descripcion) LIKE LOWER(CONCAT('%', :nombre, '%')))
          AND i.reporte.api.equipo.idEquipo = (SELECT u.equipo.idEquipo FROM Usuario u WHERE u.dni = :dniUsuario)
    """)
    Page<Issue> findByFiltersForDev(
            @Param("estados") List<String> estados,
            @Param("inicio") Timestamp inicio,
            @Param("fin") Timestamp fin,
            @Param("nombre") String nombre,
            @Param("dniUsuario") String dniUsuario,
            Pageable pageable
    );

    Integer countByEstadoNot(String estado);

    List<Issue> findTop5ByCreadorOrderByFechaCreacionDesc(Usuario creador);

}