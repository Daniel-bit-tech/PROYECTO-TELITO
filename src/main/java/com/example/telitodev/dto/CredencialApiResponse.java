package com.example.telitodev.dto;

import java.sql.Timestamp;

public class CredencialApiResponse {
    
    private Integer idCredencialApi;
    private String apiKey;
    private String nombreApi;
    private Integer apiId;
    private Timestamp fechaCreacion;
    private Boolean estado;
    private String estadoTexto;
    private String entorno;
    

    public CredencialApiResponse() {}
    
    public CredencialApiResponse(Integer idCredencialApi, String apiKey, String nombreApi, 
                               Integer apiId, Timestamp fechaCreacion, Boolean estado, String estadoTexto,String entorno) {
        this.idCredencialApi = idCredencialApi;
        this.apiKey = apiKey;
        this.nombreApi = nombreApi;
        this.apiId = apiId;
        this.fechaCreacion = fechaCreacion;
        this.estado = estado;
        this.estadoTexto = estadoTexto;
        this.entorno = entorno;
    }
    
    public Integer getIdCredencialApi() {
        return idCredencialApi;
    }
    
    public void setIdCredencialApi(Integer idCredencialApi) {
        this.idCredencialApi = idCredencialApi;
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
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
    
    public Timestamp getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(Timestamp fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public Boolean getEstado() {
        return estado;
    }
    
    public void setEstado(Boolean estado) {
        this.estado = estado;
    }
    
    public String getEstadoTexto() {
        return estadoTexto;
    }
    
    public void setEstadoTexto(String estadoTexto) {
        this.estadoTexto = estadoTexto;
    }
}