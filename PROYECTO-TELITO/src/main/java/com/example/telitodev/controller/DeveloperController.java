package com.example.telitodev.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
public class DeveloperController {

    @GetMapping("/developer")
    public String showDeveloperView() {


        return "developer";
    }
}
