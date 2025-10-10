package com.example.telitodev.service;

import com.example.telitodev.entity.Backlog;
import com.example.telitodev.entity.Feedback;
import com.example.telitodev.repository.BacklogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BacklogService {

    final BacklogRepository backlogRepository;

    public BacklogService(BacklogRepository backlogRepository) {
        this.backlogRepository = backlogRepository;
    }

    @Transactional
    public Backlog registrarFeedbackEnBacklog(Feedback feedback, String asunto, String prioridad, String estadoBacklog) {
        Backlog backlog = new Backlog();
        backlog.setDescripcion(feedback.getComentario());
        backlog.setAsunto(asunto);
        backlog.setPrioridad(prioridad);
        backlog.setEstadoBacklog(estadoBacklog);
        backlog.setApi(feedback.getApi());
        backlog.setUsuarioEncargado(feedback.getUsuario());
        backlog.setFeedback(feedback);

        return backlogRepository.save(backlog);
    }

    // ✅ NUEVO: Método para obtener backlogs con paginación y búsqueda
    public Page<Backlog> getBacklogsPaginated(String search, int page, int size) {
        // Configurar paginación: ordenar por ID descendente (más recientes primero)
        Pageable pageable = PageRequest.of(page, size, Sort.by("idBacklog").descending());

        // Si hay texto de búsqueda, usar searchBacklogs, sino usar findAll
        if (search != null && !search.trim().isEmpty()) {
            return backlogRepository.searchBacklogs(search, pageable);
        } else {
            return backlogRepository.findAll(pageable);
        }
    }

    // ✅ NUEVO: Método adicional para marcar como resuelto (si lo necesitas)
    @Transactional
    public void marcarComoResuelto(Integer idBacklog) {
        Backlog backlog = backlogRepository.findById(idBacklog)
                .orElseThrow(() -> new IllegalArgumentException("Backlog no encontrado"));
        backlog.setEstadoBacklog("Resuelto");
        backlogRepository.save(backlog);
    }
}