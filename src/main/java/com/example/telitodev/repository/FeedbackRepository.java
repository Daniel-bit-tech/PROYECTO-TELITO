package com.example.telitodev.repository;

import com.example.telitodev.entity.Api;
import com.example.telitodev.entity.Feedback;
import com.example.telitodev.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {
    List<Feedback> findByUsuario_Dni(String dni);
    List<Feedback> findByUsuario(Usuario usuario);
    List<Feedback> findByApi_Usuario_Dni(String dniUsuario);
    // NUEVO: Método que soporta paginación
    Page<Feedback> findByUsuario(Usuario usuario, Pageable pageable);
    List<Feedback> findByApiIn(List<Api> apis);



    @Modifying
    @Query("UPDATE Feedback f SET f.registradoBacklog = true WHERE f.idFeedback = :idFeedback")
    void marcarComoRegistradoEnBacklog(@Param("idFeedback") Integer idFeedback);

    List<Feedback> findByRegistradoBacklog(Boolean registradoBacklog);


}