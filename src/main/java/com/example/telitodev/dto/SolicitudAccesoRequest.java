package com.example.telitodev.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SolicitudAccesoRequest {
    
    @NotNull(message = "El ID de la API es obligatorio")
    private Integer apiId;
    
    @NotBlank(message = "El nombre del proyecto es obligatorio")
    private String nombreProyecto;
    
    @NotBlank(message = "La descripción del uso es obligatoria")
    private String descripcionUso;
    
    private String entorno = "Desarrollo"; // Por defecto desarrollo
    
    private String callbackUrl;
    
    // Constructores
    public SolicitudAccesoRequest() {}
    
    public SolicitudAccesoRequest(Integer apiId, String nombreProyecto, String descripcionUso, String entorno, String callbackUrl) {
        this.apiId = apiId;
        this.nombreProyecto = nombreProyecto;
        this.descripcionUso = descripcionUso;
        this.entorno = entorno;
        this.callbackUrl = callbackUrl;
    }
    
    // Getters y Setters
    public Integer getApiId() {
        return apiId;
    }
    
    public void setApiId(Integer apiId) {
        this.apiId = apiId;
    }
    
    public String getNombreProyecto() {
        return nombreProyecto;
    }
    
    public void setNombreProyecto(String nombreProyecto) {
        this.nombreProyecto = nombreProyecto;
    }
    
    public String getDescripcionUso() {
        return descripcionUso;
    }
    
    public void setDescripcionUso(String descripcionUso) {
        this.descripcionUso = descripcionUso;
    }
    
    public String getEntorno() {
        return entorno;
    }
    
    public void setEntorno(String entorno) {
        this.entorno = entorno;
    }
    
    public String getCallbackUrl() {
        return callbackUrl;
    }
    
    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }
    
    @Override
    public String toString() {
        return "SolicitudAccesoRequest{" +
                "apiId=" + apiId +
                ", nombreProyecto='" + nombreProyecto + '\'' +
                ", descripcionUso='" + descripcionUso + '\'' +
                ", entorno='" + entorno + '\'' +
                ", callbackUrl='" + callbackUrl + '\'' +
                '}';
    }
}