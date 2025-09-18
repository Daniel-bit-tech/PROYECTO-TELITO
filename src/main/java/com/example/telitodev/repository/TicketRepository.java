package com.example.telitodev.repository;

import com.example.telitodev.entity.Ticket;
import com.example.telitodev.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {

    List<Ticket> findByUsuario_DniOrderByFechaCreacionDesc(String usuarioDni);

    @Query("SELECT t FROM Ticket t JOIN FETCH t.usuario u JOIN FETCH t.api a WHERE t.idTicket = ?1")
    Optional<Ticket> findByIdWithDetails(int id);

}
