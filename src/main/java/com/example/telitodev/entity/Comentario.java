
package com.example.telitodev.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "comentario")
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idComentario")
    private Integer idComentario;

    @Column(name = "fecha", nullable = false)
    private Timestamp fecha;

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

    @OneToMany(mappedBy = "comentario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Adjunto> adjuntos;

    // Constructores
    public Comentario() {}

    public Comentario(Timestamp fecha, String comentario, Issue issue, Usuario usuario) {
        this.fecha = fecha;
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

    public List<Adjunto> getAdjuntos() {
        return adjuntos;
    }

    public void setAdjuntos(List<Adjunto> adjuntos) {
        this.adjuntos = adjuntos;
    }
}
