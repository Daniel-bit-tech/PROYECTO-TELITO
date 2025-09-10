package com.example.telitodev.controller.desarrollador;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DocumentacionController {



    @GetMapping("/documentacion")
    public String showLoginForm() {
        return "desarrollador/documentacion";
    }


}
