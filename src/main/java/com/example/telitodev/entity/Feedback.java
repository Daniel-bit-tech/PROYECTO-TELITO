package com.example.telitodev.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "feedback")
public class Feedback {
    
    @Id
    @Column(name = "idFeedback")
    private Integer idFeedback;
    
    @Column(name = "comentario", nullable = false, columnDefinition = "TEXT")
    private String comentario;
    
    @Column(name = "calificacion", nullable = false)
    private Integer calificacion;
    
    @Column(name = "fecha_creacion", nullable = false)
    private Timestamp fechaCreacion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuario", nullable = false, referencedColumnName = "dni")
    private Usuario usuario;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAPI", nullable = false)
    private Api api;
    
    // Constructores
    public Feedback() {}
    
    public Feedback(Integer idFeedback, String comentario, Integer calificacion,
                   Timestamp fechaCreacion, Usuario usuario, Api api) {
        this.idFeedback = idFeedback;
        this.comentario = comentario;
        this.calificacion = calificacion;
        this.fechaCreacion = fechaCreacion;
        this.usuario = usuario;
        this.api = api;
    }
    
    // Getters y Setters
    public Integer getIdFeedback() {
        return idFeedback;
    }
    
    public void setIdFeedback(Integer idFeedback) {
        this.idFeedback = idFeedback;
    }
    
    public String getComentario() {
        return comentario;
    }
    
    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
    
    public Integer getCalificacion() {
        return calificacion;
    }
    
    public void setCalificacion(Integer calificacion) {
        this.calificacion = calificacion;
    }
    
    public Timestamp getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(Timestamp fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public Usuario getUsuario() {
        return usuario;
    }
    
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    
    public Api getApi() {
        return api;
    }
    
    public void setApi(Api api) {
        this.api = api;
    }
}
