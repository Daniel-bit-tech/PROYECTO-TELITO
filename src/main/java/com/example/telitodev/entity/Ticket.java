package com.example.telitodev.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "ticket")
public class Ticket {
    
    @Id
    @Column(name = "idTicket")
    private Integer idTicket;
    
    @Column(name = "asunto", nullable = false, length = 150)
    private String asunto;
    
    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;
    
    @Column(name = "fecha_creacion", nullable = false)
    private Timestamp fechaCreacion;
    
    @Column(name = "estado", nullable = false)
    private Boolean estado;
    
    @Column(name = "fecha_final", nullable = false)
    private Timestamp fechaFinal;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuario", nullable = false)
    private Usuario usuario;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAPI", nullable = false)
    private Api api;
    
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Chatbot> chatbots;
    
    // Constructores
    public Ticket() {}
    
    public Ticket(Integer idTicket, String asunto, String descripcion, 
                 Timestamp fechaCreacion, Boolean estado, Timestamp fechaFinal,
                 Usuario usuario, Api api) {
        this.idTicket = idTicket;
        this.asunto = asunto;
        this.descripcion = descripcion;
        this.fechaCreacion = fechaCreacion;
        this.estado = estado;
        this.fechaFinal = fechaFinal;
        this.usuario = usuario;
        this.api = api;
    }
    
    // Getters y Setters
    public Integer getIdTicket() {
        return idTicket;
    }
    
    public void setIdTicket(Integer idTicket) {
        this.idTicket = idTicket;
    }
    
    public String getAsunto() {
        return asunto;
    }
    
    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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
    
    public Timestamp getFechaFinal() {
        return fechaFinal;
    }
    
    public void setFechaFinal(Timestamp fechaFinal) {
        this.fechaFinal = fechaFinal;
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
    
    public List<Chatbot> getChatbots() {
        return chatbots;
    }
    
    public void setChatbots(List<Chatbot> chatbots) {
        this.chatbots = chatbots;
    }
}
