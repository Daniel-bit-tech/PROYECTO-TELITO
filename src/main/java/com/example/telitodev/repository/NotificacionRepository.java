package com.example.telitodev.repository;

import com.example.telitodev.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {

    Integer countByUsuario_DniAndLeido(String dni, Boolean leido);
    List<Notificacion> findByUsuario_Dni(String dni);
    List<Notificacion> findByUsuario_DniAndLeido(String dni, Boolean leido);


}
