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
    Integer countByUsuario(Usuario usuario);
    List<Feedback> findByUsuario(Usuario usuario);
    // TODO: API now belongs to Equipo, not Usuario directly
    // List<Feedback> findByApi_Usuario_Dni(String dniUsuario);
    @Query("SELECT f FROM Feedback f WHERE f.api.equipo.idEquipo = (SELECT u.equipo.idEquipo FROM Usuario u WHERE u.dni = :dni)")
    List<Feedback> findByApiEquipoUsuarioDni(@Param("dni") String dni);
    // NUEVO: Método que soporta paginación
    Page<Feedback> findByUsuario(Usuario usuario, Pageable pageable);

    // Método con JOIN FETCH para evitar lazy loading exceptions
    @Query("SELECT f FROM Feedback f LEFT JOIN FETCH f.api WHERE f.usuario = :usuario")
    List<Feedback> findByUsuarioWithApi(@Param("usuario") Usuario usuario);

    // Método con paginación y JOIN FETCH
    @Query(value = "SELECT f FROM Feedback f LEFT JOIN FETCH f.api WHERE f.usuario = :usuario",
           countQuery = "SELECT COUNT(f) FROM Feedback f WHERE f.usuario = :usuario")
    Page<Feedback> findByUsuarioWithApi(@Param("usuario") Usuario usuario, Pageable pageable);

    List<Feedback> findByApiIn(List<Api> apis);

    @Modifying
    @Query("UPDATE Feedback f SET f.registradoBacklog = true WHERE f.idFeedback = :idFeedback")
    void marcarComoRegistradoEnBacklog(@Param("idFeedback") Integer idFeedback);

    List<Feedback> findByRegistradoBacklog(Boolean registradoBacklog);

    @Query("SELECT f FROM Feedback f JOIN f.usuario u WHERE u.equipo.organizacion.idOrganizacion = :organizacionId")
    List<Feedback> findByUsuarioOrganizacionId(@Param("organizacionId") Integer organizacionId);


}