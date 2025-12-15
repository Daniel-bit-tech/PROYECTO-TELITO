package com.example.telitodev.repository;

import com.example.telitodev.entity.Ticket;
import com.example.telitodev.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {

    List<Ticket> findByUsuario_DniOrderByFechaCreacionDesc(String usuarioDni);

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO ticket (asunto, descripcion, fecha_creacion, estado, idUsuario, idAPI) " +
                   "VALUES (:asunto, :descripcion, :fechaCreacion, :estado, :dni, :idApi)", 
           nativeQuery = true)
    void insertTicketNative(@Param("asunto") String asunto,
                           @Param("descripcion") String descripcion,
                           @Param("fechaCreacion") Timestamp fechaCreacion,
                           @Param("estado") int estado,
                           @Param("dni") String dni,
                           @Param("idApi") Integer idApi);


}
