package com.example.telitodev.repository;

import com.example.telitodev.entity.Roadmap;
import com.example.telitodev.entity.Roadmap.EstadoEvolucion;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {

    /* ========= CONSULTAS PARA EL TIMELINE ========= */

    // Todos los tramos de una API, ordenados cronológicamente (para la gráfica)
    List<Roadmap> findByApi_IdApiOrderByInicioAsc(Integer apiId);

    // Tramo ABIERTO (estado vigente) de una API, si existe
    Optional<Roadmap> findByApi_IdApiAndFinIsNull(Integer apiId);

    // Último tramo CERRADO (para reanudar desde el "siguiente" del flujo)
    Optional<Roadmap> findTopByApi_IdApiAndFinIsNotNullOrderByFinDesc(Integer apiId);


    /* ========= UPDATES PARA CERRAR/ABRIR TRAMOS ========= */

    // Cerrar el tramo abierto (fin = :hoy). Devuelve nº de filas afectadas (0 o 1).
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Roadmap r set r.fin = :hoy where r.api.idApi = :apiId and r.fin is null")
    int cerrarTramoAbierto(@Param("apiId") Integer apiId, @Param("hoy") LocalDate hoy);

    // Abrir un nuevo tramo (insert lo haces con save(new Roadmap(...)))
    // TIP: crea el objeto en tu Service y llama a save(r).


    /* ========= CONSULTAS ÚTILES (opcionales) ========= */

    // ¿Existe tramo abierto para esta API?
    boolean existsByApi_IdApiAndFinIsNull(Integer apiId);

    // ¿Hay algún tramo en cierto estado?
    boolean existsByApi_IdApiAndEstado(Integer apiId, EstadoEvolucion estado);

    // Visión "vigente" (todas las APIs con tramo abierto)
    List<Roadmap> findByFinIsNull();

    // Segmentos por estado (para filtrar colores, reportes, etc.)
    List<Roadmap> findByApi_IdApiAndEstadoOrderByInicioAsc(Integer apiId, EstadoEvolucion estado);
}
