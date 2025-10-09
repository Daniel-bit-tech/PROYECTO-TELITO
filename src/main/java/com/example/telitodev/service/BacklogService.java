package com.example.telitodev.service;

import com.example.telitodev.entity.Backlog;
import com.example.telitodev.entity.Feedback;
import com.example.telitodev.repository.BacklogRepository;
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
        backlog.setAsunto(asunto); // El PO podrá modificar este valor.
        backlog.setPrioridad(prioridad); // El PO podrá modificar este valor.
        backlog.setEstadoBacklog(estadoBacklog); // Estado predeterminado o el PO lo puede cambiar.
        backlog.setApi(feedback.getApi()); // Se extrae la API del feedback.
        backlog.setUsuarioEncargado(feedback.getUsuario()); // Se asigna el usuario que hizo el feedback.
        backlog.setFeedback(feedback); // Asociamos el feedback al backlog.

        // Guardar el backlog en la base de datos
        return backlogRepository.save(backlog);
    }
}
