package com.example.telitodev.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "auditlog")
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAudit")
    private Integer idAudit;
    
    @Column(name = "entidad", nullable = false, length = 60)
    private String entidad;
    
    @Column(name = "idEntidad", nullable = false)
    private Integer idEntidad;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "accion", nullable = false)
    private AccionAudit accion;
    
    @Column(name = "cambios", columnDefinition = "JSON")
    private String cambios;
    
    @Column(name = "fecha", insertable = false, updatable = false)
    private Timestamp fecha;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuario", referencedColumnName = "dni")
    private Usuario usuario;
    
    public enum AccionAudit {
        INSERT, UPDATE, DELETE
    }
    
    // Constructores
    public AuditLog() {}
    
    public AuditLog(String entidad, Integer idEntidad, AccionAudit accion, 
                    String cambios, Usuario usuario) {
        this.entidad = entidad;
        this.idEntidad = idEntidad;
        this.accion = accion;
        this.cambios = cambios;
        this.usuario = usuario;
    }
    
    // Getters y Setters
    public Integer getIdAudit() {
        return idAudit;
    }
    
    public void setIdAudit(Integer idAudit) {
        this.idAudit = idAudit;
    }
    
    public String getEntidad() {
        return entidad;
    }
    
    public void setEntidad(String entidad) {
        this.entidad = entidad;
    }
    
    public Integer getIdEntidad() {
        return idEntidad;
    }
    
    public void setIdEntidad(Integer idEntidad) {
        this.idEntidad = idEntidad;
    }
    
    public AccionAudit getAccion() {
        return accion;
    }
    
    public void setAccion(AccionAudit accion) {
        this.accion = accion;
    }
    
    public String getCambios() {
        return cambios;
    }
    
    public void setCambios(String cambios) {
        this.cambios = cambios;
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
