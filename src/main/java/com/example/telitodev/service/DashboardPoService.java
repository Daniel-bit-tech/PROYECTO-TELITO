package com.example.telitodev.service;


import com.example.telitodev.dto.BacklogResumenDTO;
import com.example.telitodev.dto.DashboardPoDTO;
import com.example.telitodev.dto.FeedbackRecienteDTO;
import com.example.telitodev.entity.*;
import com.example.telitodev.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardPoService {

    @Autowired private ProyectoRepository proyectoRepository;
    @Autowired private ProyectoHasApiRepository proyectoHasApiRepository;
    @Autowired private MetricaApiRepository metricaApiRepository;
    @Autowired private FeedbackRepository feedbackRepository;
    @Autowired private BacklogRepository backlogRepository;
    @Autowired private RoadmapRepository roadmapRepository;

    public DashboardPoDTO getDashboardData(Integer idOrganizacion) {

        DashboardPoDTO dto = new DashboardPoDTO();

        List<Proyecto> proyectos = proyectoRepository.findByEquipoOrganizacionId(idOrganizacion);
        List<Api> apis = proyectoHasApiRepository.findApisByProyectosIn(proyectos);

        dto.setTotalProyectos(proyectos.size());
        dto.setApisUtilizadas(apis.size());

        if (apis.isEmpty()) {
            return dto;
        }

        List<MetricaApi> metricas = metricaApiRepository.findByApiIn(apis);

        long totalLlamadas = metricas.stream().mapToLong(MetricaApi::getLlamadas).sum();
        dto.setTotalLlamadas(totalLlamadas);

        int totalErrores = metricas.stream().mapToInt(MetricaApi::getErrores).sum();
        dto.setTotalErrores(totalErrores);

        double costoTotal = metricas.stream().mapToDouble(m -> m.getCosto().doubleValue()).sum();
        dto.setCostoTotal(costoTotal);

        double latenciaPromedio = metricas.stream().mapToInt(MetricaApi::getLatenciaPromedio).average().orElse(0.0);
        dto.setLatenciaPromedio(latenciaPromedio);

        Map<String, Long> llamadasPorDia = metricas.stream()
                .collect(Collectors.groupingBy(
                        m -> m.getFecha().toInstant().toString().substring(0, 10),
                        Collectors.summingLong(MetricaApi::getLlamadas)
                ));
        dto.setLlamadasPorDia(llamadasPorDia);

        List<Roadmap> roadmapItems = roadmapRepository.findByApiIn(apis);
        Map<String, Long> roadmapStatus = roadmapItems.stream()
                .collect(Collectors.groupingBy(Roadmap::getEstado, Collectors.counting()));
        dto.setRoadmapStatus(roadmapStatus);


        List<Feedback> feedbacks = feedbackRepository.findByApiIn(apis);
        List<FeedbackRecienteDTO> feedbackDTOs = feedbacks.stream()
                .map(f -> new FeedbackRecienteDTO(
                        f.getApi().getNombre(),
                        f.getCalificacion(),
                        f.getComentario(),
                        f.getFechaCreacion().toInstant()))
                .limit(5)
                .collect(Collectors.toList());
        dto.setFeedbackRecientes(feedbackDTOs);

        List<Backlog> backlogs = backlogRepository.findByApiIn(apis);
        List<BacklogResumenDTO> backlogDTOs = backlogs.stream()
                .map(b -> new BacklogResumenDTO(
                        b.getApi().getNombre(),
                        b.getAsunto(),
                        b.getPrioridad(),
                        b.getEstadoBacklog()))
                .limit(5)
                .collect(Collectors.toList());
        dto.setBacklogActivos(backlogDTOs);

        return dto;
    }
}

