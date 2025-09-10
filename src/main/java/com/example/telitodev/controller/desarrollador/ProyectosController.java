package com.example.telitodev.controller.desarrollador;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProyectosController {

    @GetMapping("/proyectos")
    public String showsand() {
        return "desarrollador/proyectos";
    }

}
