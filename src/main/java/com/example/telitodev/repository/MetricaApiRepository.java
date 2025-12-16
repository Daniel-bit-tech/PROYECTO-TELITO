package com.example.telitodev.repository;

import com.example.telitodev.entity.Api;
import com.example.telitodev.entity.MetricaApi;
import com.example.telitodev.entity.ProyectoHasApi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Repository
public interface MetricaApiRepository extends JpaRepository<MetricaApi, Integer> {

    List<MetricaApi> findByApiIdApi(Integer idApi);

    /* ---- (a) Listado con filtros opcionales ---- */
    @Query("""
        SELECT m FROM MetricaApi m
        WHERE (:idApi IS NULL OR m.api.idApi = :idApi)
          AND (:idEntorno IS NULL OR m.entorno.idEntorno = :idEntorno)
          AND (:start IS NULL OR m.fecha >= :start)
          AND (:end   IS NULL OR m.fecha <= :end)
        ORDER BY m.fecha DESC
    """)
    List<MetricaApi> search(
            @Param("idApi") Integer idApi,
            @Param("idEntorno") Integer idEntorno,
            @Param("start") Timestamp start,
            @Param("end")   Timestamp end
    );

    /* ---- (b) Serie para Chart.js: latencia promedio por API ---- */
    @Query("""
        SELECT m.api.nombre, ROUND(AVG(m.latenciaPromedio))
        FROM MetricaApi m
        WHERE (:idApi IS NULL OR m.api.idApi = :idApi)
          AND (:idEntorno IS NULL OR m.entorno.idEntorno = :idEntorno)
          AND (:start IS NULL OR m.fecha >= :start)
          AND (:end   IS NULL OR m.fecha <= :end)
        GROUP BY m.api.nombre
        ORDER BY 2 DESC
    """)
    List<Object[]> avgLatencyByApi(
            @Param("idApi") Integer idApi,
            @Param("idEntorno") Integer idEntorno,
            @Param("start") Timestamp start,
            @Param("end")   Timestamp end
    );

    /* ---- (c) Agregados para KPIs ---- */
    @Query("SELECT COALESCE(SUM(m.llamadas), 0) FROM MetricaApi m")
    Long sumLlamadas();

    @Query("SELECT COALESCE(SUM(m.errores), 0) FROM MetricaApi m")
    Long sumErrores();

    @Query("SELECT COALESCE(AVG(m.latenciaPromedio), 0) FROM MetricaApi m")
    Double avgLatencia();

    @Query("SELECT COALESCE(SUM(m.llamadas), 0) FROM MetricaApi m")
    Long sumLlamadasGlobal();

    @Query("SELECT COALESCE(SUM(m.errores), 0) FROM MetricaApi m")
    Long sumErroresGlobal();

    @Query("SELECT COALESCE(AVG(m.latenciaPromedio), 0.0) FROM MetricaApi m")
    Double avgLatenciaGlobal();

    /* ---- (d) Donut: Éxito vs Errores ---- */
    @Query("""
        SELECT 
          COALESCE(SUM(m.llamadas - m.errores), 0) AS exitos,
          COALESCE(SUM(m.errores), 0)             AS errores
        FROM MetricaApi m
        WHERE (:idApi IS NULL OR m.api.idApi = :idApi)
          AND (:idEntorno IS NULL OR m.entorno.idEntorno = :idEntorno)
          AND (:start IS NULL OR m.fecha >= :start)
          AND (:end   IS NULL OR m.fecha <= :end)
    """)
    Object[] successVsErrors(
            @Param("idApi") Integer idApi,
            @Param("idEntorno") Integer idEntorno,
            @Param("start") Timestamp start,
            @Param("end")   Timestamp end
    );

    /* ===== CONSULTAS ADICIONALES PARA KPIs AVANZADOS ===== */
    @Query(value = """
        SELECT 
            HOUR(m.fecha) as hora,
            SUM(m.llamadas) as requests
        FROM metricaapi m 
        WHERE m.fecha >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
        GROUP BY HOUR(m.fecha)
        ORDER BY hora
    """, nativeQuery = true)
    List<Object[]> getThroughputByHour();

    @Query(value = """
        SELECT 
            DATE(m.fecha) as fecha,
            SUM(m.llamadas) as requests
        FROM metricaapi m 
        WHERE m.fecha >= DATE_SUB(NOW(), INTERVAL 30 DAY)
        GROUP BY DATE(m.fecha)
        ORDER BY fecha
    """, nativeQuery = true)
    List<Object[]> getThroughputByDay();

    @Query(value = """
        SELECT 
            a.nombre,
            SUM(m.llamadas) as totalRequests,
            AVG(m.latencia_promedio) as avgLatency,
            CASE 
                WHEN SUM(m.llamadas) = 0 THEN 0.0
                ELSE (SUM(m.errores) * 100.0 / SUM(m.llamadas))
            END as errorRate
        FROM metricaapi m 
        JOIN api a ON m.idAPI = a.idAPI
        GROUP BY a.nombre
        HAVING SUM(m.llamadas) > 0
        ORDER BY totalRequests DESC
        LIMIT 5
    """, nativeQuery = true)
    List<Object[]> getTopUsedApis();

    @Query(value = """
        SELECT 
            a.nombre,
            SUM(m.costo) as totalCost,
            AVG(m.costo) as avgCost,
            SUM(m.llamadas) as requests
        FROM metricaapi m 
        JOIN api a ON m.idAPI = a.idAPI
        WHERE m.costo IS NOT NULL
        GROUP BY a.nombre
        ORDER BY totalCost DESC
    """, nativeQuery = true)
    List<Object[]> getCostMetricsByApi();

    @Query(value = """
        SELECT 
            CASE 
                WHEN SUM(m.llamadas) = 0 THEN 100.0
                ELSE (SUM(CASE WHEN m.errores = 0 THEN m.llamadas ELSE 0 END) * 100.0 / SUM(m.llamadas))
            END as availability
        FROM metricaapi m 
        WHERE m.fecha >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
    """, nativeQuery = true)
    Double getSystemAvailability();

    @Query(value = """
        SELECT 
            DATE(m.fecha) as fecha,
            AVG(m.latencia_promedio) as avgLatency
        FROM metricaapi m 
        WHERE m.fecha >= DATE_SUB(NOW(), INTERVAL 7 DAY)
        GROUP BY DATE(m.fecha)
        ORDER BY fecha
    """, nativeQuery = true)
    List<Object[]> getLatencyTrend();

    @Query(value = """
        SELECT 
            COALESCE(SUM(m.llamadas), 0) / 60.0 as requestsPerMinute
        FROM metricaapi m 
        WHERE m.fecha >= DATE_SUB(NOW(), INTERVAL 1 HOUR)
    """, nativeQuery = true)
    Double getCurrentThroughput();

    @Query(value = """
        SELECT 
            e.nombre,
            SUM(m.llamadas) as requests,
            (SUM(m.llamadas) * 100.0 / (SELECT SUM(mm.llamadas) FROM metricaapi mm)) as percentage
        FROM metricaapi m 
        JOIN entorno e ON m.idEntorno = e.idEntorno
        GROUP BY e.nombre
        ORDER BY requests DESC
    """, nativeQuery = true)
    List<Object[]> getUsageByEnvironment();

    List<MetricaApi> findByApiIn(List<Api> apis);

    //=============================================================================================
    // --- NUEVOS MÉTODOS PARA CÁLCULOS POR RANGO ---

    @Query("""
        SELECT COALESCE(AVG(m.latenciaPromedio), 0.0)
        FROM MetricaApi m
        WHERE m.latenciaPromedio IS NOT NULL
          AND (:start IS NULL OR m.fecha >= :start)
          AND (:end IS NULL OR m.fecha < :end)
          AND (:idApi IS NULL OR m.api.idApi = :idApi)
          AND (:idEntorno IS NULL OR m.entorno.idEntorno = :idEntorno)
    """)
    Double avgLatenciaBetween(
            @Param("start") Timestamp start,
            @Param("end") Timestamp end,
            @Param("idApi") Integer idApi,
            @Param("idEntorno") Integer idEntorno
    );

    @Query("""
        SELECT COALESCE(SUM(m.llamadas), 0L)
        FROM MetricaApi m
        WHERE m.llamadas IS NOT NULL
          AND (:start IS NULL OR m.fecha >= :start)
          AND (:end IS NULL OR m.fecha < :end)
          AND (:idApi IS NULL OR m.api.idApi = :idApi)
          AND (:idEntorno IS NULL OR m.entorno.idEntorno = :idEntorno)
    """)
    Long sumLlamadasBetween(
            @Param("start") Timestamp start,
            @Param("end") Timestamp end,
            @Param("idApi") Integer idApi,
            @Param("idEntorno") Integer idEntorno
    );

    @Query("""
        SELECT COALESCE(SUM(m.errores), 0L)
        FROM MetricaApi m
        WHERE m.errores IS NOT NULL
          AND (:start IS NULL OR m.fecha >= :start)
          AND (:end IS NULL OR m.fecha < :end)
          AND (:idApi IS NULL OR m.api.idApi = :idApi)
          AND (:idEntorno IS NULL OR m.entorno.idEntorno = :idEntorno)
    """)
    Long sumErroresBetween(
            @Param("start") Timestamp start,
            @Param("end") Timestamp end,
            @Param("idApi") Integer idApi,
            @Param("idEntorno") Integer idEntorno
    );

    @Query("""
        SELECT COALESCE(SUM(m.costo), 0.0)
        FROM MetricaApi m
        WHERE m.costo IS NOT NULL
          AND (:start IS NULL OR m.fecha >= :start)
          AND (:end IS NULL OR m.fecha < :end)
          AND (:idApi IS NULL OR m.api.idApi = :idApi)
          AND (:idEntorno IS NULL OR m.entorno.idEntorno = :idEntorno)
    """)
    BigDecimal sumCostoBetween(
            @Param("start") Timestamp start,
            @Param("end") Timestamp end,
            @Param("idApi") Integer idApi,
            @Param("idEntorno") Integer idEntorno
    );

    @Query(value = """
        SELECT COUNT(DISTINCT DATE_FORMAT(m.fecha, '%Y-%m-%d %H:%i'))
        FROM metricaapi m
        WHERE m.llamadas > 0
          AND (:start IS NULL OR m.fecha >= :start)
          AND (:end IS NULL OR m.fecha < :end)
          AND (:idApi IS NULL OR m.idAPI = :idApi)
          AND (:idEntorno IS NULL OR m.idEntorno = :idEntorno)
    """, nativeQuery = true)
    Long countMinutesWithCallsBetween(
            @Param("start") Timestamp start,
            @Param("end") Timestamp end,
            @Param("idApi") Integer idApi,
            @Param("idEntorno") Integer idEntorno
    );

    //=============================================================================================
    // ✅ NUEVO: TODO FILTRADO POR ORGANIZACIÓN (NO ROMPE LO EXISTENTE)

    // Subquery base: APIs pertenecientes a la organización via ProyectoHasApi -> Proyecto -> Equipo -> Organizacion
    // NOTA: esto hace match con tu modelo Organización -> Equipo -> Proyecto -> ProyectoHasApi -> Api

    @Query("""
      SELECT COALESCE(SUM(m.llamadas),0)
      FROM MetricaApi m
      WHERE m.fecha BETWEEN :start AND :end
        AND (:idApi IS NULL OR m.api.idApi = :idApi)
        AND (:idEntorno IS NULL OR m.entorno.idEntorno = :idEntorno)
        AND m.api.idApi IN (
          SELECT DISTINCT pha.api.idApi
          FROM ProyectoHasApi pha
          WHERE pha.proyecto.equipo.organizacion.idOrganizacion = :orgId
        )
    """)
    Long sumLlamadasBetweenByOrg(
            @Param("start") Timestamp start,
            @Param("end") Timestamp end,
            @Param("orgId") Integer orgId,
            @Param("idApi") Integer idApi,
            @Param("idEntorno") Integer idEntorno
    );

    @Query("""
      SELECT COALESCE(SUM(m.errores),0)
      FROM MetricaApi m
      WHERE m.fecha BETWEEN :start AND :end
        AND (:idApi IS NULL OR m.api.idApi = :idApi)
        AND (:idEntorno IS NULL OR m.entorno.idEntorno = :idEntorno)
        AND m.api.idApi IN (
          SELECT DISTINCT pha.api.idApi
          FROM ProyectoHasApi pha
          WHERE pha.proyecto.equipo.organizacion.idOrganizacion = :orgId
        )
    """)
    Long sumErroresBetweenByOrg(
            @Param("start") Timestamp start,
            @Param("end") Timestamp end,
            @Param("orgId") Integer orgId,
            @Param("idApi") Integer idApi,
            @Param("idEntorno") Integer idEntorno
    );

    @Query("""
      SELECT COALESCE(AVG(m.latenciaPromedio),0)
      FROM MetricaApi m
      WHERE m.fecha BETWEEN :start AND :end
        AND (:idApi IS NULL OR m.api.idApi = :idApi)
        AND (:idEntorno IS NULL OR m.entorno.idEntorno = :idEntorno)
        AND m.api.idApi IN (
          SELECT DISTINCT pha.api.idApi
          FROM ProyectoHasApi pha
          WHERE pha.proyecto.equipo.organizacion.idOrganizacion = :orgId
        )
    """)
    Double avgLatenciaBetweenByOrg(
            @Param("start") Timestamp start,
            @Param("end") Timestamp end,
            @Param("orgId") Integer orgId,
            @Param("idApi") Integer idApi,
            @Param("idEntorno") Integer idEntorno
    );

    @Query("""
      SELECT m.api.nombre, ROUND(AVG(m.latenciaPromedio))
      FROM MetricaApi m
      WHERE m.fecha BETWEEN :start AND :end
        AND (:idApi IS NULL OR m.api.idApi = :idApi)
        AND (:idEntorno IS NULL OR m.entorno.idEntorno = :idEntorno)
        AND m.api.idApi IN (
          SELECT DISTINCT pha.api.idApi
          FROM ProyectoHasApi pha
          WHERE pha.proyecto.equipo.organizacion.idOrganizacion = :orgId
        )
      GROUP BY m.api.nombre
      ORDER BY 2 DESC
    """)
    List<Object[]> avgLatencyByApiByOrg(
            @Param("orgId") Integer orgId,
            @Param("idApi") Integer idApi,
            @Param("idEntorno") Integer idEntorno,
            @Param("start") Timestamp start,
            @Param("end") Timestamp end
    );

    @Query("""
      SELECT 
        COALESCE(SUM(m.llamadas - m.errores), 0) AS exitos,
        COALESCE(SUM(m.errores), 0)             AS errores
      FROM MetricaApi m
      WHERE m.fecha BETWEEN :start AND :end
        AND (:idApi IS NULL OR m.api.idApi = :idApi)
        AND (:idEntorno IS NULL OR m.entorno.idEntorno = :idEntorno)
        AND m.api.idApi IN (
          SELECT DISTINCT pha.api.idApi
          FROM ProyectoHasApi pha
          WHERE pha.proyecto.equipo.organizacion.idOrganizacion = :orgId
        )
    """)
    Object[] successVsErrorsByOrg(
            @Param("orgId") Integer orgId,
            @Param("idApi") Integer idApi,
            @Param("idEntorno") Integer idEntorno,
            @Param("start") Timestamp start,
            @Param("end") Timestamp end
    );

    // --- TOP APIs por org (JPQL, sin depender de tablas) ---
    @Query("""
      SELECT m.api.nombre,
             COALESCE(SUM(m.llamadas),0),
             COALESCE(AVG(m.latenciaPromedio),0),
             CASE WHEN COALESCE(SUM(m.llamadas),0) = 0 THEN 0.0
                  ELSE (COALESCE(SUM(m.errores),0) * 100.0 / COALESCE(SUM(m.llamadas),0))
             END
      FROM MetricaApi m
      WHERE m.api.idApi IN (
        SELECT DISTINCT pha.api.idApi
        FROM ProyectoHasApi pha
        WHERE pha.proyecto.equipo.organizacion.idOrganizacion = :orgId
      )
      GROUP BY m.api.nombre
      HAVING COALESCE(SUM(m.llamadas),0) > 0
      ORDER BY COALESCE(SUM(m.llamadas),0) DESC
    """)
    List<Object[]> getTopUsedApisByOrg(@Param("orgId") Integer orgId);

    // --- Costos por API por org ---
    @Query("""
      SELECT m.api.nombre,
             COALESCE(SUM(m.costo),0),
             COALESCE(AVG(m.costo),0),
             COALESCE(SUM(m.llamadas),0)
      FROM MetricaApi m
      WHERE m.costo IS NOT NULL
        AND m.api.idApi IN (
          SELECT DISTINCT pha.api.idApi
          FROM ProyectoHasApi pha
          WHERE pha.proyecto.equipo.organizacion.idOrganizacion = :orgId
        )
      GROUP BY m.api.nombre
      ORDER BY COALESCE(SUM(m.costo),0) DESC
    """)
    List<Object[]> getCostMetricsByApiByOrg(@Param("orgId") Integer orgId);

    // --- Uso por entorno por org (sin percentage; el % lo calculas en service) ---
    @Query("""
      SELECT m.entorno.nombre, COALESCE(SUM(m.llamadas),0)
      FROM MetricaApi m
      WHERE m.api.idApi IN (
        SELECT DISTINCT pha.api.idApi
        FROM ProyectoHasApi pha
        WHERE pha.proyecto.equipo.organizacion.idOrganizacion = :orgId
      )
      GROUP BY m.entorno.nombre
      ORDER BY COALESCE(SUM(m.llamadas),0) DESC
    """)
    List<Object[]> getUsageByEnvironmentByOrgRaw(@Param("orgId") Integer orgId);

    // --- Throughput por hora por org (últimas 24h) ---
    @Query("""
      SELECT FUNCTION('HOUR', m.fecha), COALESCE(SUM(m.llamadas),0)
      FROM MetricaApi m
      WHERE m.fecha >= :since
        AND m.api.idApi IN (
          SELECT DISTINCT pha.api.idApi
          FROM ProyectoHasApi pha
          WHERE pha.proyecto.equipo.organizacion.idOrganizacion = :orgId
        )
      GROUP BY FUNCTION('HOUR', m.fecha)
      ORDER BY FUNCTION('HOUR', m.fecha)
    """)
    List<Object[]> getThroughputByHourByOrg(
            @Param("orgId") Integer orgId,
            @Param("since") Timestamp since
    );

    // --- Tendencia latencia (7 días) por org ---
    @Query("""
      SELECT FUNCTION('DATE', m.fecha), COALESCE(AVG(m.latenciaPromedio),0)
      FROM MetricaApi m
      WHERE m.fecha >= :since
        AND m.api.idApi IN (
          SELECT DISTINCT pha.api.idApi
          FROM ProyectoHasApi pha
          WHERE pha.proyecto.equipo.organizacion.idOrganizacion = :orgId
        )
      GROUP BY FUNCTION('DATE', m.fecha)
      ORDER BY FUNCTION('DATE', m.fecha)
    """)
    List<Object[]> getLatencyTrendByOrg(
            @Param("orgId") Integer orgId,
            @Param("since") Timestamp since
    );

    // --- Throughput actual (última hora) por org ---
    @Query("""
      SELECT (COALESCE(SUM(m.llamadas),0) / 60.0)
      FROM MetricaApi m
      WHERE m.fecha >= :since
        AND m.api.idApi IN (
          SELECT DISTINCT pha.api.idApi
          FROM ProyectoHasApi pha
          WHERE pha.proyecto.equipo.organizacion.idOrganizacion = :orgId
        )
    """)
    Double getCurrentThroughputByOrg(
            @Param("orgId") Integer orgId,
            @Param("since") Timestamp since
    );

    // --- Disponibilidad (24h) por org ---
    @Query("""
      SELECT CASE
               WHEN COALESCE(SUM(m.llamadas),0) = 0 THEN 100.0
               ELSE (COALESCE(SUM(CASE WHEN m.errores = 0 THEN m.llamadas ELSE 0 END),0) * 100.0 / COALESCE(SUM(m.llamadas),0))
             END
      FROM MetricaApi m
      WHERE m.fecha >= :since
        AND m.api.idApi IN (
          SELECT DISTINCT pha.api.idApi
          FROM ProyectoHasApi pha
          WHERE pha.proyecto.equipo.organizacion.idOrganizacion = :orgId
        )
    """)
    Double getSystemAvailabilityByOrg(
            @Param("orgId") Integer orgId,
            @Param("since") Timestamp since
    );
}
