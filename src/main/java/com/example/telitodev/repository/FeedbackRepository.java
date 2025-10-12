package com.example.telitodev.repository;

import com.example.telitodev.entity.Feedback;
import com.example.telitodev.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {
    List<Feedback> findByUsuario_Dni(String dni);
    List<Feedback> findByUsuario(Usuario usuario);
}