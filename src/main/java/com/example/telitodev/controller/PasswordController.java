package com.example.telitodev.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PasswordController {


    @GetMapping("/forgot-password")
    public String mostrarFormulario() {
        return "sesion/olvidarpass";
    }


    @PostMapping("/forgot-password")
    public String procesarRecuperacion(@RequestParam("email") String email, Model model) {

        return "sesion/olvidarpass";
    }
}

