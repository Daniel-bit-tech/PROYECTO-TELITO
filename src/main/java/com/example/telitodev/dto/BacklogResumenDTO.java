package com.example.telitodev.dto;

public class BacklogResumenDTO {
    private String apiNombre;
    private String asunto;
    private String prioridad;
    private String estado;

    public BacklogResumenDTO(String apiNombre, String asunto, String prioridad, String estado) {
        this.apiNombre = apiNombre;
        this.asunto = asunto;
        this.prioridad = prioridad;
        this.estado = estado;
    }


    public String getApiNombre() { return apiNombre; }
    public void setApiNombre(String apiNombre) { this.apiNombre = apiNombre; }
    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }
    public String getPrioridad() { return prioridad; }
    public void setPrioridad(String prioridad) { this.prioridad = prioridad; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}