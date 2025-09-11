package com.example.telitodev.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "solicitudacceso")
public class SolicitudAcceso {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idSolicitudAcceso")
    private Integer idSolicitudAcceso;
    
    @Column(name = "estado", nullable = false)
    private Boolean estado;
    
    @Column(name = "fecha_solicitud", nullable = false)
    private Timestamp fechaSolicitud;
    
    @Column(name = "fecha_respuesta", nullable = false)
    private Timestamp fechaRespuesta;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuario", nullable = false, referencedColumnName = "dni")
    private Usuario usuario;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAPI", nullable = false)
    private Api api;
    
    // Constructores
    public SolicitudAcceso() {}
    
    public SolicitudAcceso(Boolean estado, Timestamp fechaSolicitud, 
                          Timestamp fechaRespuesta, Usuario usuario, Api api) {
        this.estado = estado;
        this.fechaSolicitud = fechaSolicitud;
        this.fechaRespuesta = fechaRespuesta;
        this.usuario = usuario;
        this.api = api;
    }
    
    // Getters y Setters
    public Integer getIdSolicitudAcceso() {
        return idSolicitudAcceso;
    }
    
    public void setIdSolicitudAcceso(Integer idSolicitudAcceso) {
        this.idSolicitudAcceso = idSolicitudAcceso;
    }
    
    public Boolean getEstado() {
        return estado;
    }
    
    public void setEstado(Boolean estado) {
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
