package com.example.telitodev.service;

import com.example.telitodev.entity.Backlog;
import com.example.telitodev.entity.Feedback;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.BacklogRepository;
import com.example.telitodev.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedbackService {

    private final BacklogRepository backlogRepository;
    private final FeedbackRepository feedbackRepository;

    @Autowired
    public FeedbackService(BacklogRepository backlogRepository, FeedbackRepository feedbackRepository) {
        this.backlogRepository = backlogRepository;
        this.feedbackRepository = feedbackRepository;
    }

    @Transactional
    public Backlog registrarFeedbackEnBacklog(Integer idFeedback, String asunto, Usuario usuario) throws Exception {
        // Recuperar el feedback por ID
        Feedback feedback = feedbackRepository.findById(idFeedback)
                .orElseThrow(() -> new Exception("Feedback no encontrado"));

        // Calcular la prioridad del feedback
        String prioridad = calcularPrioridad(feedback);

        // Crear un nuevo backlog
        Backlog backlog = new Backlog();
        backlog.setDescripcion(feedback.getComentario());
        backlog.setAsunto(asunto);  // El PO podrá modificar este valor
        backlog.setPrioridad(prioridad);  // El PO podrá modificar este valor
        backlog.setEstadoBacklog("desarrollo");  // Estado predeterminado
        backlog.setApi(feedback.getApi());  // Asignamos la API relacionada con el feedback
        backlog.setUsuarioEncargado(usuario);  // Asignamos el usuario que está registrando el feedback
        backlog.setFeedback(feedback);  // Asociamos el feedback al backlog

        // Guardamos el backlog en la base de datos
        return backlogRepository.save(backlog);
    }

    private String calcularPrioridad(Feedback feedback) {
        // Establecer la prioridad según la calificación del feedback
        double calificacion = feedback.getCalificacion();
        if (calificacion >= 1 && calificacion <= 2) {
            return "Baja";
        } else if (calificacion == 3) {
            return "Media";
        } else if (calificacion >= 4 && calificacion <= 5) {
            return "Alta";
        }
        return "Media"; // Valor por defecto si no se encuentra la calificación
    }
}
