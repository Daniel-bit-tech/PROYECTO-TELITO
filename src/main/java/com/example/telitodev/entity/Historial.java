package com.example.telitodev.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "historial")
public class Historial {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idHistorial")
    private Integer idHistorial;
    
    @Column(name = "fecha", nullable = false)
    private Timestamp fecha;
    
    @Column(name = "etiqueta", nullable = false, length = 25)
    private String etiqueta;
    
    @Column(name = "descripción", length = 200)
    private String descripcion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "idIssue", referencedColumnName = "idIssue", nullable = false),
        @JoinColumn(name = "idReporte", referencedColumnName = "idReporte", nullable = false)
    })
    private Issue issue;
    
    // Constructores
    public Historial() {}
    
    public Historial(Timestamp fecha, String etiqueta, String descripcion, Issue issue) {
        this.fecha = fecha;
        this.etiqueta = etiqueta;
        this.descripcion = descripcion;
        this.issue = issue;
    }
    
    // Getters y Setters
    public Integer getIdHistorial() {
        return idHistorial;
    }
    
    public void setIdHistorial(Integer idHistorial) {
        this.idHistorial = idHistorial;
    }
    
    public Timestamp getFecha() {
        return fecha;
    }
    
    public void setFecha(Timestamp fecha) {
        this.fecha = fecha;
    }
    
    public String getEtiqueta() {
        return etiqueta;
    }
    
    public void setEtiqueta(String etiqueta) {
        this.etiqueta = etiqueta;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public Issue getIssue() {
        return issue;
    }
    
    public void setIssue(Issue issue) {
        this.issue = issue;
    }
}
