package com.example.telitodev.dto;

import java.time.Instant;


public class FeedbackRecienteDTO {
    private String apiNombre;
    private int calificacion;
    private String comentario;
    private Instant fecha;

    public FeedbackRecienteDTO(String apiNombre, int calificacion, String comentario, Instant fecha) {
        this.apiNombre = apiNombre;
        this.calificacion = calificacion;
        this.comentario = comentario;
        this.fecha = fecha;
    }


    public String getApiNombre() { return apiNombre; }
    public void setApiNombre(String apiNombre) { this.apiNombre = apiNombre; }
    public int getCalificacion() { return calificacion; }
    public void setCalificacion(int calificacion) { this.calificacion = calificacion; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public Instant getFecha() { return fecha; }
    public void setFecha(Instant fecha) { this.fecha = fecha; }
}