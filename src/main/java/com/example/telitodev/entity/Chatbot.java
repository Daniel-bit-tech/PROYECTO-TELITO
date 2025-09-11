package com.example.telitodev.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "chatbot")
public class Chatbot {
    
    @Id
    @Column(name = "idChatBOT")
    private Integer idChatBot;
    
    @Column(name = "pregunta", columnDefinition = "TEXT")
    private String pregunta;
    
    @Column(name = "respuesta", columnDefinition = "TEXT")
    private String respuesta;
    
    @Column(name = "fecha")
    private Timestamp fecha;
    
    @Column(name = "estado", length = 45)
    private String estado;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuario", nullable = false, referencedColumnName = "dni")
    private Usuario usuario;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTicket")
    private Ticket ticket;
    
    // Constructores
    public Chatbot() {}
    
    public Chatbot(Integer idChatBot, String pregunta, String respuesta,
                  Timestamp fecha, String estado, Usuario usuario, Ticket ticket) {
        this.idChatBot = idChatBot;
        this.pregunta = pregunta;
        this.respuesta = respuesta;
        this.fecha = fecha;
        this.estado = estado;
        this.usuario = usuario;
        this.ticket = ticket;
    }
    
    // Getters y Setters
    public Integer getIdChatBot() {
        return idChatBot;
    }
    
    public void setIdChatBot(Integer idChatBot) {
        this.idChatBot = idChatBot;
    }
    
    public String getPregunta() {
        return pregunta;
    }
    
    public void setPregunta(String pregunta) {
        this.pregunta = pregunta;
    }
    
    public String getRespuesta() {
        return respuesta;
    }
    
    public void setRespuesta(String respuesta) {
        this.respuesta = respuesta;
    }
    
    public Timestamp getFecha() {
        return fecha;
    }
    
    public void setFecha(Timestamp fecha) {
        this.fecha = fecha;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public Usuario getUsuario() {
        return usuario;
    }
    
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    
    public Ticket getTicket() {
        return ticket;
    }
    
    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }
}
