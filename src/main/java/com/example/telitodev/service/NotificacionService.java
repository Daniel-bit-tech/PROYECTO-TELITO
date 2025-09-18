package com.example.telitodev.service;



import com.example.telitodev.entity.Notificacion;
import com.example.telitodev.repository.NotificacionRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;

    public NotificacionService(NotificacionRepository notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

    public Notificacion guardarNotificacion(Notificacion notificacion) {
        return notificacionRepository.save(notificacion);
    }

    public List<Notificacion> obtenerTodasNotificaciones() {
        return notificacionRepository.findAll();
    }

    public Optional<Notificacion> obtenerNotificacionPorId(Integer id) {
        return notificacionRepository.findById(id);
    }

    public void eliminarNotificacion(Integer id) {
        notificacionRepository.deleteById(id);
    }


    public List<Notificacion> obtenerNotificacionesPorUsuario(String dni) {
        return notificacionRepository.findByUsuario_Dni(dni);
    }


    public List<Notificacion> obtenerNotificacionesNoLeidasPorUsuario(String dni) {
        return notificacionRepository.findByUsuario_DniAndLeido(dni, false);
    }


    public Integer contarNotificacionesNoLeidas(String dni) {
        return notificacionRepository.countByUsuario_DniAndLeido(dni, false);
    }
}
