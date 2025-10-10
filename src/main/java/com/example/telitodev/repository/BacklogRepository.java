package com.example.telitodev.repository;

import com.example.telitodev.entity.Backlog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface BacklogRepository extends JpaRepository<Backlog, Integer> {

    // Método existente - mantenerlo
    Optional<Backlog> findByFeedback_IdFeedback(Integer idFeedback);

    // ✅ NUEVO: Paginación básica
    Page<Backlog> findAll(Pageable pageable);

    // ✅ NUEVO: Búsqueda en múltiples campos
    @Query("SELECT b FROM Backlog b WHERE " +
            "b.api.nombre LIKE %:search% OR " +
            "b.usuarioEncargado.nombre LIKE %:search% OR " +
            "b.descripcion LIKE %:search% OR " +
            "b.prioridad LIKE %:search% OR " +
            "b.estadoBacklog LIKE %:search% OR " +
            "b.asunto LIKE %:search%")
    Page<Backlog> searchBacklogs(@Param("search") String search, Pageable pageable);
}