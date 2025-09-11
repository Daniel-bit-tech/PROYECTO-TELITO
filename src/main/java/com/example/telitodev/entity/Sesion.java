package com.example.telitodev.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "sesion")
public class Sesion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idSesion")
    private Integer idSesion;
    
    @Column(name = "token", nullable = false, length = 255)
    private String token;
    
    @Column(name = "tipo_autenticacion", nullable = false, length = 255)
    private String tipoAutenticacion;
    
    @Column(name = "fecha_inicio")
    private Timestamp fechaInicio;
    
    @Column(name = "fecha_expiracion")
    private Timestamp fechaExpiracion;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoSesion estado;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuario", nullable = false, referencedColumnName = "dni")
    private Usuario usuario;
    
    public enum EstadoSesion {
        Activa, Expirada, Revocada
    }
    
    // Constructores
    public Sesion() {}
    
    public Sesion(String token, String tipoAutenticacion, Timestamp fechaInicio, 
                  Timestamp fechaExpiracion, EstadoSesion estado, Usuario usuario) {
        this.token = token;
        this.tipoAutenticacion = tipoAutenticacion;
        this.fechaInicio = fechaInicio;
        this.fechaExpiracion = fechaExpiracion;
        this.estado = estado;
        this.usuario = usuario;
    }
    
    // Getters y Setters
    public Integer getIdSesion() {
        return idSesion;
    }
    
    public void setIdSesion(Integer idSesion) {
        this.idSesion = idSesion;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getTipoAutenticacion() {
        return tipoAutenticacion;
    }
    
    public void setTipoAutenticacion(String tipoAutenticacion) {
        this.tipoAutenticacion = tipoAutenticacion;
    }
    
    public Timestamp getFechaInicio() {
        return fechaInicio;
    }
    
    public void setFechaInicio(Timestamp fechaInicio) {
        this.fechaInicio = fechaInicio;
    }
    
    public Timestamp getFechaExpiracion() {
        return fechaExpiracion;
    }
    
    public void setFechaExpiracion(Timestamp fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }
    
    public EstadoSesion getEstado() {
        return estado;
    }
    
    public void setEstado(EstadoSesion estado) {
        this.estado = estado;
    }
    
    public Usuario getUsuario() {
        return usuario;
    }
    
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
