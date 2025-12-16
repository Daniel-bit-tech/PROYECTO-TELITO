package com.example.telitodev.repository;

import com.example.telitodev.entity.Api;
import com.example.telitodev.entity.Roadmap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoadmapRepository extends JpaRepository<Roadmap, Integer> {

    // Encontrar roadmap por API ID
    Optional<Roadmap> findFirstByApiIdApiOrderByFechaModificacionDesc(Integer apiId);

    // Encontrar roadmap por API ID (sin ordenar)
    Optional<Roadmap> findByApiIdApi(Integer apiId);

    // Encontrar roadmaps por una lista de APIs
    List<Roadmap> findByApiIn(List<Api> apis);

    // Encontrar todos los roadmaps ordenados por fecha de modificación descendente
    List<Roadmap> findAllByOrderByFechaModificacionDesc();

    // Encontrar roadmaps por estado
    List<Roadmap> findByEstado(String estado);

    // Encontrar roadmaps por estado ordenados por fecha de modificación
    List<Roadmap> findByEstadoOrderByFechaModificacionDesc(String estado);

    // Verificar si existe un roadmap para una API específica
    boolean existsByApiIdApi(Integer apiId);

    // Actualizar estado de una API específica
    @Modifying
    @Transactional
    @Query("UPDATE Roadmap r SET r.estado = :estado, r.fechaModificacion = CURRENT_TIMESTAMP WHERE r.api.idApi = :apiId")
    void updateEstadoByApiId(@Param("apiId") Integer apiId, @Param("estado") String estado);

    // Contar APIs por estado
    @Query("SELECT r.estado, COUNT(r) FROM Roadmap r GROUP BY r.estado")
    List<Object[]> countByEstado();

    // Encontrar todas las APIs con su estado (incluso las que no tienen roadmap)
    @Query("SELECT a.idApi, a.nombre, COALESCE(r.estado, 'Sin estado') as estado, " +
            "COALESCE(r.fechaModificacion, CURRENT_TIMESTAMP) as fechaModificacion " +
            "FROM Api a LEFT JOIN Roadmap r ON a.idApi = r.api.idApi " +
            "ORDER BY a.nombre")
    List<Object[]> findAllApisWithEstado();
}