package com.example.telitodev.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "evidencia")
public class Evidencia {
    
    @EmbeddedId
    private EvidenciaId id;
    
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;
    
    @Column(name = "evidencia", nullable = false, length = 200)
    private String evidencia;
    
    @Lob
    @Column(name = "descripción")
    private byte[] descripcion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "idIssue", referencedColumnName = "idIssue", nullable = false),
        @JoinColumn(name = "idReporte", referencedColumnName = "idReporte", nullable = false)
    })
    private Issue issue;
    
    // Constructores
    public Evidencia() {}
    
    public Evidencia(String nombre, String evidencia, byte[] descripcion, Issue issue) {
        this.nombre = nombre;
        this.evidencia = evidencia;
        this.descripcion = descripcion;
        this.issue = issue;
        if (issue != null && issue.getId() != null) {
            this.id = new EvidenciaId(null, issue.getId().getIdIssue(), issue.getId().getIdReporte());
        }
    }
    
    // Getters y Setters
    public EvidenciaId getId() {
        return id;
    }
    
    public void setId(EvidenciaId id) {
        this.id = id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getEvidencia() {
        return evidencia;
    }
    
    public void setEvidencia(String evidencia) {
        this.evidencia = evidencia;
    }
    
    public byte[] getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(byte[] descripcion) {
        this.descripcion = descripcion;
    }
    
    public Issue getIssue() {
        return issue;
    }
    
    public void setIssue(Issue issue) {
        this.issue = issue;
    }
}
