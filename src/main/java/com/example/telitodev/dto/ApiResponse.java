package com.example.telitodev.dto;

import com.example.telitodev.entity.Dominio;
import com.example.telitodev.entity.Tag;

public class ApiResponse {
    
    private Integer idApi;
    private String nombre;
    private String descripcion;
    private Dominio dominio;
    private Tag tipoApi;     // tag es tipoApi?
    private String endpointUrl;
    
    // Constructores
    public ApiResponse() {}
    
    public ApiResponse(Integer idApi, String nombre, String descripcion, Dominio dominio, Tag tipoApi, String endpointUrl) {
        this.idApi = idApi;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.dominio = dominio;
        this.tipoApi = tipoApi;
        this.endpointUrl = endpointUrl;
    }
    
    // Getters y Setters
    public Integer getIdApi() {
        return idApi;
    }
    
    public void setIdApi(Integer idApi) {
        this.idApi = idApi;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public Dominio getDominio() {
        return dominio;
    }
    
    public void setDominio(Dominio dominio) {
        this.dominio = dominio;
    }
    
    public Tag getTipoApi() {
        return tipoApi;
    }
    
    public void setTipoApi(Tag tipoApi) {
        this.tipoApi = tipoApi;
    }
    
    public String getEndpointUrl() {
        return endpointUrl;
    }
    
    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }
}