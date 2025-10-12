package com.example.telitodev.service;

import com.example.telitodev.dto.OrganizacionStatsDTO;
import com.example.telitodev.entity.Organizacion;
import com.example.telitodev.repository.OrganizacionRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OrganizacionService {

    private final OrganizacionRepository organizacionRepository;

    public OrganizacionService(OrganizacionRepository organizacionRepository) {
        this.organizacionRepository = organizacionRepository;
    }

    public Organizacion obtenerOrganizacionPorUsuario(String dniUsuario) {
        return organizacionRepository.findByUsuarioDni(dniUsuario);
    }

    public OrganizacionStatsDTO obtenerEstadisticasOrganizacion(Integer idOrganizacion) {
        Optional<Organizacion> organizacionOpt = organizacionRepository.findById(idOrganizacion);

        if (organizacionOpt.isPresent()) {
            Organizacion organizacion = organizacionOpt.get();

            // Calcular estadísticas
            Long proyectosActivos = organizacion.getProyectos().stream()
                    .filter(proyecto -> Boolean.TRUE.equals(proyecto.getActivo()))
                    .count();

            Long miembros = (long) organizacion.getUsuarios().size();

            Long apisAsociadas = organizacion.getProyectos().stream()
                    .flatMap(proyecto -> proyecto.getProyectoHasApis().stream())
                    .map(proyectoHasApi -> proyectoHasApi.getApi().getIdApi())
                    .distinct()
                    .count();

            return new OrganizacionStatsDTO(
                    organizacion.getNombre(),
                    organizacion.getDescripcion(),
                    organizacion.getFechaCreacion(),
                    proyectosActivos,
                    apisAsociadas,
                    miembros
            );
        }

        return null;
    }
}