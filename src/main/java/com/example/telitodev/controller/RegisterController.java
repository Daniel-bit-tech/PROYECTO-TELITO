package com.example.telitodev.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegisterController {

    @GetMapping("/register")
    public String showRegisterForm() {
        return "sesion/register";
    }

    @PostMapping("/register")
    public String processRegistration(){

        return "sesion/login";
    }
}

