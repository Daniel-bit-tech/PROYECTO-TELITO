package com.example.telitodev.entity;


import jakarta.persistence.*;

@Entity
@Table(name = "backlog")
public class Backlog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idBacklog")
    private Integer idBacklog;

    @Column(name = "descripcion", nullable = true)
    private String descripcion;

    @Column(name = "asunto", nullable = true, length = 50)
    private String asunto;

    @Column(name = "prioridad", nullable = true, length = 50)
    private String prioridad;

    @Column(name = "estado_backlog", nullable = true, length = 50)
    private String estadoBacklog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_api", referencedColumnName = "idAPI", nullable = false)
    private Api api;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_encargado", referencedColumnName = "dni", nullable = false)
    private Usuario usuarioEncargado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_feedback", referencedColumnName = "idFeedback", nullable = true)
    private Feedback feedback;

    // Constructores
    public Backlog() {}

    public Backlog(String descripcion, String asunto, String prioridad, String estadoBacklog, Api api, Usuario usuarioEncargado, Feedback feedback) {
        this.descripcion = descripcion;
        this.asunto = asunto;
        this.prioridad = prioridad;
        this.estadoBacklog = estadoBacklog;
        this.api = api;
        this.usuarioEncargado = usuarioEncargado;
        this.feedback = feedback;
    }

    // Getters y Setters
    public Integer getIdBacklog() {
        return idBacklog;
    }

    public void setIdBacklog(Integer idBacklog) {
        this.idBacklog = idBacklog;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }

    public String getEstadoBacklog() {
        return estadoBacklog;
    }

    public void setEstadoBacklog(String estadoBacklog) {
        this.estadoBacklog = estadoBacklog;
    }

    public Api getApi() {
        return api;
    }

    public void setApi(Api api) {
        this.api = api;
    }

    public Usuario getUsuarioEncargado() {
        return usuarioEncargado;
    }

    public void setUsuarioEncargado(Usuario usuarioEncargado) {
        this.usuarioEncargado = usuarioEncargado;
    }

    public Feedback getFeedback() {
        return feedback;
    }

    public void setFeedback(Feedback feedback) {
        this.feedback = feedback;
    }
}
