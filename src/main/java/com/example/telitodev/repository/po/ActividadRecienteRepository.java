package com.example.telitodev.repository.po;

import com.example.telitodev.entity.ActividadReciente;
import com.example.telitodev.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActividadRecienteRepository extends JpaRepository<ActividadReciente, Integer> {


    List<ActividadReciente> findTop5ByUsuario_DniOrderByFechaDesc(String dni);
    List<ActividadReciente> findTop5ByUsuarioOrderByFechaDesc(Usuario usuario);
    List<ActividadReciente> findTop10ByUsuarioOrderByFechaDesc(Usuario usuario);
}
