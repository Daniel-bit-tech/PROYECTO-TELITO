package com.example.telitodev.dto;

import java.sql.Timestamp;

public class SolicitudAccesoResponse {
    
    private Integer idSolicitudAcceso;
    private String nombreApi;
    private Integer apiId;
    private String estado; // "PENDIENTE", "APROBADA", "RECHAZADA"
    private Timestamp fechaSolicitud;
    private Timestamp fechaRespuesta;
    private String nombreProyecto;
    private String descripcionUso;
    
    // Constructores
    public SolicitudAccesoResponse() {}
    
    public SolicitudAccesoResponse(Integer idSolicitudAcceso, String nombreApi, Integer apiId, 
                                 String estado, Timestamp fechaSolicitud, Timestamp fechaRespuesta,
                                 String nombreProyecto, String descripcionUso) {
        this.idSolicitudAcceso = idSolicitudAcceso;
        this.nombreApi = nombreApi;
        this.apiId = apiId;
        this.estado = estado;
        this.fechaSolicitud = fechaSolicitud;
        this.fechaRespuesta = fechaRespuesta;
        this.nombreProyecto = nombreProyecto;
        this.descripcionUso = descripcionUso;
    }
    
    // Getters y Setters
    public Integer getIdSolicitudAcceso() {
        return idSolicitudAcceso;
    }
    
    public void setIdSolicitudAcceso(Integer idSolicitudAcceso) {
        this.idSolicitudAcceso = idSolicitudAcceso;
    }
    
    public String getNombreApi() {
        return nombreApi;
    }
    
    public void setNombreApi(String nombreApi) {
        this.nombreApi = nombreApi;
    }
    
    public Integer getApiId() {
        return apiId;
    }
    
    public void setApiId(Integer apiId) {
        this.apiId = apiId;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public Timestamp getFechaSolicitud() {
        return fechaSolicitud;
    }
    
    public void setFechaSolicitud(Timestamp fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }
    
    public Timestamp getFechaRespuesta() {
        return fechaRespuesta;
    }
    
    public void setFechaRespuesta(Timestamp fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
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
}