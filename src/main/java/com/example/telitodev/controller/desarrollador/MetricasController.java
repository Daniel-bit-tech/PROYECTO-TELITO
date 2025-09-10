package com.example.telitodev.controller.desarrollador;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MetricasController {

    @GetMapping("/metricas")
    public String showmetricas() {
        return "desarrollador/metricas";
    }

}
