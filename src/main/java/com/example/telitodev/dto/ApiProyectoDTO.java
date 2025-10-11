package com.example.telitodev.dto;

import java.sql.Timestamp;

public class ApiProyectoDTO {

    private Integer idApi;
    private String nombreApi;
    private String nombreProyecto;
    private String descripcion;
    private String endpointUrl;
    private String nombreDominio;
    private String nombreTag;
    private Timestamp fechaCreacion;

    // Constructor con parámetros
    public ApiProyectoDTO(Integer idApi, String nombreApi, String nombreProyecto, String descripcion, String endpointUrl,
                          String nombreDominio, String nombreTag, Timestamp fechaCreacion) {
        this.idApi = idApi;
        this.nombreApi = nombreApi;
        this.nombreProyecto = nombreProyecto;
        this.descripcion = descripcion;
        this.endpointUrl = endpointUrl;
        this.nombreDominio = nombreDominio;
        this.nombreTag = nombreTag;
        this.fechaCreacion = fechaCreacion;
    }

    // Getters y setters
    public String getNombreApi() {
        return nombreApi;
    }

    public void setNombreApi(String nombreApi) {
        this.nombreApi = nombreApi;
    }

    public String getNombreProyecto() {
        return nombreProyecto;
    }

    public void setNombreProyecto(String nombreProyecto) {
        this.nombreProyecto = nombreProyecto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    public String getNombreDominio() {
        return nombreDominio;
    }

    public void setNombreDominio(String nombreDominio) {
        this.nombreDominio = nombreDominio;
    }

    public String getNombreTag() {
        return nombreTag;
    }

    public void setNombreTag(String nombreTag) {
        this.nombreTag = nombreTag;
    }

    public Timestamp getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Timestamp fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }


    public Integer getIdApi() {
        return idApi;
    }

    public void setIdApi(Integer idApi) {
        this.idApi = idApi;
    }
}