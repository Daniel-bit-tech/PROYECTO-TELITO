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
    private String desarrollador; // Nombre del desarrollador
    private String email; // Email del desarrollador
    private String fechaFormatted; // Fecha formateada para mostrar
    private String nombreEquipo;
    // Constructores
    public SolicitudAccesoResponse() {}

    public SolicitudAccesoResponse(Integer idSolicitudAcceso, String nombreApi, Integer apiId,
                                   String estado, Timestamp fechaSolicitud, Timestamp fechaRespuesta,
                                   String nombreProyecto, String descripcionUso, String desarrollador,
                                   String email, String fechaFormatted,String nombreEquipo) {
        this.idSolicitudAcceso = idSolicitudAcceso;
        this.nombreApi = nombreApi;
        this.apiId = apiId;
        this.estado = estado;
        this.fechaSolicitud = fechaSolicitud;
        this.fechaRespuesta = fechaRespuesta;
        this.nombreProyecto = nombreProyecto;
        this.descripcionUso = descripcionUso;
        this.desarrollador = desarrollador;
        this.email = email;
        this.fechaFormatted = fechaFormatted;
        this.nombreEquipo = nombreEquipo;
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

    public String getDesarrollador() {
        return desarrollador;
    }

    public void setDesarrollador(String desarrollador) {
        this.desarrollador = desarrollador;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFechaFormatted() {
        return fechaFormatted;
    }

    public void setFechaFormatted(String fechaFormatted) {
        this.fechaFormatted = fechaFormatted;
    }

    public String getNombreEquipo() {
        return nombreEquipo;
    }

    public void setNombreEquipo(String nombreEquipo) {
        this.nombreEquipo = nombreEquipo;
    }
}