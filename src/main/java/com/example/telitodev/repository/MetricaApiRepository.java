package com.example.telitodev.repository;

import com.example.telitodev.entity.MetricaApi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}

