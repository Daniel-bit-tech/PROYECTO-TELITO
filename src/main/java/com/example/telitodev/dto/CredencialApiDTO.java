package com.example.telitodev.dto;

import java.sql.Timestamp;

public class CredencialApiDTO {
    private Integer idCredencialApi;
    private String apiKey;
    private Timestamp fechaCreacion;
    private Boolean estado;
    private String apiNombre;
    private Integer apiId;
    private String apiVersion;
    
    public CredencialApiDTO() {}
    
    public CredencialApiDTO(Integer idCredencialApi, String apiKey, Timestamp fechaCreacion, 
                           Boolean estado, String apiNombre, Integer apiId, String apiVersion) {
        this.idCredencialApi = idCredencialApi;
        this.apiKey = apiKey;
        this.fechaCreacion = fechaCreacion;
        this.estado = estado;
        this.apiNombre = apiNombre;
        this.apiId = apiId;
        this.apiVersion = apiVersion;
    }

    // Getters y Setters
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

    public String getApiNombre() {
        return apiNombre;
    }

    public void setApiNombre(String apiNombre) {
        this.apiNombre = apiNombre;
    }

    public Integer getApiId() {
        return apiId;
    }

    public void setApiId(Integer apiId) {
        this.apiId = apiId;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }
}
