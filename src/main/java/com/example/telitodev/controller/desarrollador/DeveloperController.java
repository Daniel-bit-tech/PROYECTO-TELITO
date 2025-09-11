package com.example.telitodev.controller.desarrollador;

import com.example.telitodev.repository.ApiRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping("/dev")
@PreAuthorize("hasRole('DEV')")
public class DeveloperController {


    @GetMapping("/home")
    public String showDeveloperView() {
        System.out.println("entro");




        return "desarrollador/developer";
    }

    @GetMapping("/catalogo")
    public String developerDashboard(Model model, Authentication authentication) {
        model.addAttribute("smg", authentication.getName());





        return "desarrollador/apis";
    }
}
