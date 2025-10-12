package com.example.telitodev.service;

import com.example.telitodev.dto.OrganizacionStatsDTO;
import com.example.telitodev.entity.Organizacion;
import com.example.telitodev.repository.OrganizacionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrganizacionService {

    private final OrganizacionRepository organizacionRepository;

    public OrganizacionService(OrganizacionRepository organizacionRepository) {
        this.organizacionRepository = organizacionRepository;
    }

    // Nuevo para buscar organización por ID (útil para la vista "Solicitudes de Acceso" del PO)

    public Optional<Organizacion> obtenerOrganizacionPorId(Integer id) {
        return organizacionRepository.findById(id);
    }

    public List<Organizacion> obtenerTodasOrganizacionesOrdenadas() {
        return organizacionRepository.findAllOrderByNombre();
    }

    public Optional<Organizacion> obtenerOrganizacionPorNombre(String nombre) {
        return organizacionRepository.findByNombre(nombre);
    }

    public boolean existeOrganizacionPorNombre(String nombre) {
        return organizacionRepository.existsByNombre(nombre);
    }

    public List<Organizacion> obtenerOrganizacionesPorIds(List<Integer> ids) {
        return organizacionRepository.findByIdOrganizacionIn(ids);
    }

    public List<Object[]> obtenerEstadisticasUsuariosPorOrganizacion() {
        return organizacionRepository.countUsuariosPorOrganizacion();
    }

}