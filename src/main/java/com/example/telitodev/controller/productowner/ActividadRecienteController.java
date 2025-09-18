package com.example.telitodev.controller.productowner;



import com.example.telitodev.entity.ActividadReciente;
import com.example.telitodev.service.ActividadRecienteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

@Controller
@RequestMapping("/po/actividad")
public class ActividadRecienteController {

    private final ActividadRecienteService actividadRecienteService;

    public ActividadRecienteController(ActividadRecienteService actividadRecienteService) {
        this.actividadRecienteService = actividadRecienteService;
    }


    @GetMapping
    public String listarActividades(Model model) {
        List<ActividadReciente> listaActividades = actividadRecienteService.obtenerTodasActividades();
        model.addAttribute("listaActividades", listaActividades);
        return "po/actividad-reciente";
    }
}
