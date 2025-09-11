package com.example.telitodev.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "comentario")
public class Comentario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idComentario")
    private Integer idComentario;
    
    @Column(name = "fecha", nullable = false)
    private Timestamp fecha;
    
    @Lob
    @Column(name = "adjunto")
    private byte[] adjunto;
    
    @Column(name = "comentario", nullable = false, length = 400)
    private String comentario;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "idIssue", referencedColumnName = "idIssue", nullable = false),
        @JoinColumn(name = "idReporte", referencedColumnName = "idReporte", nullable = false)
    })
    private Issue issue;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuario", nullable = false, referencedColumnName = "dni")
    private Usuario usuario;
    
    // Constructores
    public Comentario() {}
    
    public Comentario(Timestamp fecha, byte[] adjunto, String comentario, Issue issue, Usuario usuario) {
        this.fecha = fecha;
        this.adjunto = adjunto;
        this.comentario = comentario;
        this.issue = issue;
        this.usuario = usuario;
    }
    
    // Getters y Setters
    public Integer getIdComentario() {
        return idComentario;
    }
    
    public void setIdComentario(Integer idComentario) {
        this.idComentario = idComentario;
    }
    
    public Timestamp getFecha() {
        return fecha;
    }
    
    public void setFecha(Timestamp fecha) {
        this.fecha = fecha;
    }
    
    public byte[] getAdjunto() {
        return adjunto;
    }
    
    public void setAdjunto(byte[] adjunto) {
        this.adjunto = adjunto;
    }
    
    public String getComentario() {
        return comentario;
    }
    
    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
    
    public Issue getIssue() {
        return issue;
    }
    
    public void setIssue(Issue issue) {
        this.issue = issue;
    }
    
    public Usuario getUsuario() {
        return usuario;
    }
    
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
