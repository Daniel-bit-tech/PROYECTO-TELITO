package com.example.telitodev.repository;

import com.example.telitodev.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvidenciaRepository extends JpaRepository<Evidencia, Integer> {
    // Método para encontrar todas las evidencias asociadas a un reporte
    List<Evidencia> findByReporte(Reporte reporte);
    List<Evidencia> findByReporteIdReporte(Integer idReporte);
}