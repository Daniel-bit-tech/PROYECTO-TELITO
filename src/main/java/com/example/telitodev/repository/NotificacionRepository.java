package com.example.telitodev.repository;

import com.example.telitodev.entity.Notificacion;
import com.example.telitodev.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {

    Integer countByUsuario_DniAndLeido(String dni, Boolean leido);
    List<Notificacion> findByUsuario_Dni(String dni);
    List<Notificacion> findByUsuario_DniAndLeido(String dni, Boolean leido);
    Integer countByUsuarioAndLeido(Usuario usuario, Boolean leido);

    // Últimas 5 notificaciones no leídas
    List<Notificacion> findTop5ByUsuarioDniAndLeidoOrderByFechaDesc(String dni, Boolean leido);

    // Todas las notificaciones leídas y no leídas con paginación
    Page<Notificacion> findByUsuarioDniOrderByFechaDesc(String dni, Pageable pageable);

}
