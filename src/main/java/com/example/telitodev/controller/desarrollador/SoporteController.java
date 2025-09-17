package com.example.telitodev.controller.desarrollador;


import com.example.telitodev.entity.Ticket;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.TicketRepository;
import com.example.telitodev.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class SoporteController {

    final UsuarioRepository usuarioRepository;
    final TicketRepository ticketRepository;
    public SoporteController(UsuarioRepository usuarioRepository, TicketRepository ticketRepository) {
        this.usuarioRepository = usuarioRepository;
        this.ticketRepository = ticketRepository;
    }

    @GetMapping("/soporte")
    public String showsoporte(Model model, Authentication auth) {

        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());


        List<Ticket> tickets = ticketRepository.findByUsuario_DniOrderByFechaCreacionDesc(usuario.getDni());
        model.addAttribute("tickets", tickets);







        model.addAttribute("usuario", usuario);

        return "desarrollador/soporte";
    }

}
