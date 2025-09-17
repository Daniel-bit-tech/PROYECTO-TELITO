package com.example.telitodev.repository;

import com.example.telitodev.entity.Ticket;
import com.example.telitodev.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {

    List<Ticket> findByUsuario_DniOrderByFechaCreacionDesc(String usuarioDni);

}
