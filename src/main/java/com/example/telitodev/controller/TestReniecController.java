package com.example.telitodev.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador para la página de prueba de la integración RENIEC
 */
@Controller
public class TestReniecController {

    @GetMapping("/test-reniec")
    public String testReniec() {
        return "test-reniec";
    }
}
