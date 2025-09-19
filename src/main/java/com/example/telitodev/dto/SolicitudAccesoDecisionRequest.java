package com.example.telitodev.dto;

import jakarta.validation.constraints.NotBlank;

public class SolicitudAccesoDecisionRequest {
    
    @NotBlank(message = "La acción es obligatoria")
    private String accion; // "APROBAR" o "RECHAZAR"
    
    private String motivoRechazo; // Solo requerido si se rechaza
    
    // Constructores
    public SolicitudAccesoDecisionRequest() {}
    
    public SolicitudAccesoDecisionRequest(String accion, String motivoRechazo) {
        this.accion = accion;
        this.motivoRechazo = motivoRechazo;
    }
    
    // Getters y Setters
    public String getAccion() {
        return accion;
    }
    
    public void setAccion(String accion) {
        this.accion = accion;
    }
    
    public String getMotivoRechazo() {
        return motivoRechazo;
    }
    
    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }
}