package com.example.telitodev.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "reporte")
public class Reporte {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idReporte")
    private Integer idReporte;
    
    @Column(name = "descripcion", length = 200)
    private String descripcion;
    
    @Column(name = "fecha_creacion", nullable = false)
    private Timestamp fechaCreacion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "API_idAPI", nullable = false)
    private Api api;
    
    @OneToMany(mappedBy = "reporte", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Issue> issues;
    
    // Constructores
    public Reporte() {}
    
    public Reporte(String descripcion, Timestamp fechaCreacion, Api api) {
        this.descripcion = descripcion;
        this.fechaCreacion = fechaCreacion;
        this.api = api;
    }
    
    // Getters y Setters
    public Integer getIdReporte() {
        return idReporte;
    }
    
    public void setIdReporte(Integer idReporte) {
        this.idReporte = idReporte;
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
    
    public Api getApi() {
        return api;
    }
    
    public void setApi(Api api) {
        this.api = api;
    }
    
    public List<Issue> getIssues() {
        return issues;
    }
    
    public void setIssues(List<Issue> issues) {
        this.issues = issues;
    }
}
