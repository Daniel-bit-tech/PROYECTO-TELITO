package com.example.telitodev.controller.desarrollador;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SoporteController {

    @GetMapping("/soporte")
    public String showsoporte() {
        return "desarrollador/soporte";
    }

}
