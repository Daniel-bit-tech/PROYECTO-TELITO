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




}
