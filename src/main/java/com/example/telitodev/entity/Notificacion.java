package com.example.telitodev.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "notificacion")
public class Notificacion {
    
    @Id
    @Column(name = "idNotificacion")
    private Integer idNotificacion;
    
    @Column(name = "mensaje", nullable = false, columnDefinition = "TEXT")
    private String mensaje;
    
    @Column(name = "leido", nullable = false)
    private Boolean leido;
    
    @Column(name = "fecha", nullable = false)
    private Timestamp fecha;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuario", nullable = false, referencedColumnName = "dni")
    private Usuario usuario;
    
    // Constructores
    public Notificacion() {}
    
    public Notificacion(Integer idNotificacion, String mensaje, Boolean leido, 
                       Timestamp fecha, Usuario usuario) {
        this.idNotificacion = idNotificacion;
        this.mensaje = mensaje;
        this.leido = leido;
        this.fecha = fecha;
        this.usuario = usuario;
    }
    
    // Getters y Setters
    public Integer getIdNotificacion() {
        return idNotificacion;
    }
    
    public void setIdNotificacion(Integer idNotificacion) {
        this.idNotificacion = idNotificacion;
    }
    
    public String getMensaje() {
        return mensaje;
    }
    
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
    
    public Boolean getLeido() {
        return leido;
    }
    
    public void setLeido(Boolean leido) {
        this.leido = leido;
    }
    
    public Timestamp getFecha() {
        return fecha;
    }
    
    public void setFecha(Timestamp fecha) {
        this.fecha = fecha;
    }
    
    public Usuario getUsuario() {
        return usuario;
    }
    
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
