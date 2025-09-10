package com.example.telitodev.controller.desarrollador;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OnboardingController {

    @GetMapping("/onboarding")
    public String showonb() {
        return "desarrollador/onboarding";
    }

}
