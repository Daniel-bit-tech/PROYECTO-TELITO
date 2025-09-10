package com.example.telitodev.controller.desarrollador;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PerfilController {

    @GetMapping("/perfil")
    public String showperfil() {
        return "desarrollador/perfil";
    }

}