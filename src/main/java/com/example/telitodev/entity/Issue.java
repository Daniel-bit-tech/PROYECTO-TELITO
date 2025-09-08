package com.example.telitodev.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "issue")
public class Issue {
    
    @EmbeddedId
    private IssueId id;
    
    @Column(name = "estado", nullable = false, length = 25)
    private String estado = "Reportado";
    
    @Column(name = "descripcion", length = 200)
    private String descripcion;
    
    @Column(name = "fecha_creacion", nullable = false)
    private Timestamp fechaCreacion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idReporte")
    @JoinColumn(name = "idReporte")
    private Reporte reporte;
    
    @OneToMany(mappedBy = "issue", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comentario> comentarios;
    
    @OneToMany(mappedBy = "issue", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Evidencia> evidencias;
    
    @OneToMany(mappedBy = "issue", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Historial> historiales;
    
    // Constructores
    public Issue() {}
    
    public Issue(String descripcion, Timestamp fechaCreacion, Reporte reporte) {
        this.descripcion = descripcion;
        this.fechaCreacion = fechaCreacion;
        this.reporte = reporte;
    }
    
    // Getters y Setters
    public IssueId getId() {
        return id;
    }
    
    public void setId(IssueId id) {
        this.id = id;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
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
    
    public Reporte getReporte() {
        return reporte;
    }
    
    public void setReporte(Reporte reporte) {
        this.reporte = reporte;
    }
    
    public List<Comentario> getComentarios() {
        return comentarios;
    }
    
    public void setComentarios(List<Comentario> comentarios) {
        this.comentarios = comentarios;
    }
    
    public List<Evidencia> getEvidencias() {
        return evidencias;
    }
    
    public void setEvidencias(List<Evidencia> evidencias) {
        this.evidencias = evidencias;
    }
    
    public List<Historial> getHistoriales() {
        return historiales;
    }
    
    public void setHistoriales(List<Historial> historiales) {
        this.historiales = historiales;
    }
}
