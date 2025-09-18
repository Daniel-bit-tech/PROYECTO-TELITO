package com.example.telitodev.service;

import com.example.telitodev.entity.ActividadReciente;
import com.example.telitodev.repository.po.ActividadRecienteRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ActividadRecienteService {

    private final ActividadRecienteRepository actividadRecienteRepository;

    public ActividadRecienteService(ActividadRecienteRepository actividadRecienteRepository) {
        this.actividadRecienteRepository = actividadRecienteRepository;
    }

    public ActividadReciente guardarActividad(ActividadReciente actividad) {
        return actividadRecienteRepository.save(actividad);
    }

    public List<ActividadReciente> obtenerTodasActividades() {
        return actividadRecienteRepository.findAll();
    }

    public Optional<ActividadReciente> obtenerActividadPorId(Integer id) {
        return actividadRecienteRepository.findById(id);
    }

    public void eliminarActividad(Integer id) {
        actividadRecienteRepository.deleteById(id);
    }

    public List<ActividadReciente> obtenerActividadesRecientesPorUsuario(String dni) {
        return actividadRecienteRepository.findTop5ByUsuario_DniOrderByFechaDesc(dni);
    }
}
