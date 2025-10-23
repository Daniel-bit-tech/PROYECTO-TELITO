package com.example.telitodev.repository;

import com.example.telitodev.entity.Api;
import com.example.telitodev.entity.Backlog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface BacklogRepository extends JpaRepository<Backlog, Integer> {
    Optional<Backlog> findByFeedback_IdFeedback(Integer idFeedback);

    // Métodos para búsqueda con paginación
    @Query("SELECT b FROM Backlog b WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(b.api.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(b.descripcion) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(b.usuarioEncargado.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(b.prioridad) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(b.estadoBacklog) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Backlog> findBySearchTerm(@Param("search") String search, Pageable pageable);

    // Método adicional para contar total de elementos con búsqueda
    @Query("SELECT COUNT(b) FROM Backlog b WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(b.api.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(b.descripcion) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(b.usuarioEncargado.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(b.prioridad) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(b.estadoBacklog) LIKE LOWER(CONCAT('%', :search, '%')))")
    long countBySearchTerm(@Param("search") String search);

    List<Backlog> findByApiIn(List<Api> apis);

}