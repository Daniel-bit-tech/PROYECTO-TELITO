package com.example.telitodev.service;

import com.example.telitodev.entity.Ticket;
import com.example.telitodev.repository.TicketRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public Ticket guardarTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    public List<Ticket> obtenerTodosTickets() {
        return ticketRepository.findAll();
    }

    public Optional<Ticket> obtenerTicketPorId(Integer id) {
        return ticketRepository.findById(id);
    }

    public void eliminarTicket(Integer id) {
        ticketRepository.deleteById(id);
    }


    public List<Ticket> obtenerTicketsPorUsuario(String dni) {
        return ticketRepository.findByUsuario_DniOrderByFechaCreacionDesc(dni);
    }
}
