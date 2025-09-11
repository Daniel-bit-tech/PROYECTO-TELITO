package com.example.telitodev.controller.productowner;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PoController {


    @GetMapping("/documentacion")
    public String showDocumentacionView() {
        return "productOwner/documentacion";
    }

    @GetMapping("/KPIs")
    public String showKPIsView() {
        return "productOwner/KPIs";
    }
    @GetMapping("/bandejaSolicitud")
    public String showbandejaSolicitudesView() {
        return "productOwner/bandejaSolicitud";
    }
    @GetMapping("/versolicitud")
    public String showSolicitudesView(){
        return "productOwner/verSolicitud";
    }
    @GetMapping("/backlog")
    public String showBacklogView() {
        return "productOwner/backlog";
    }
    @GetMapping("/feedback")
    public String showFeedbackView() {
        return "productOwner/feedback";
    }
    @GetMapping("/roadmap")
    public String showRoadmapView() {
        return "productOwner/roadmap";
    }
    @GetMapping("/roadmapGestion")
    public String showRoadmapGestionView() {
        return "productOwner/roadmapGestion";
    }
    @GetMapping("/roadmapDetalle")
    public String showRoadmapDetalleView() {
        return "productOwner/roadmapDetalle";
    }
    @GetMapping("/feedbackDetalle")
    public String showfeedbackDetalleView() {
        return "productOwner/feedbackDetalle";
    }
    @GetMapping("/Dashboard")
    public String showDashboardView() {
        return "productOwner/Dashboard";
    }
    @GetMapping("/verPerfil")
    public String showverPerfilView() {
        return "productOwner/verPerfil";
    }
    @GetMapping("/verSolicitud")
    public String showverSolicitudView() {
        return "productOwner/verSolicitud";
    }

}
