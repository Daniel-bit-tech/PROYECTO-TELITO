package com.example.telitodev.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "evidencia")
public class Evidencia {
    
    @EmbeddedId
    private EvidenciaId id;
    
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Lob
    @Column(name = "evidencia", nullable = false)
    private byte[] evidencia;
    
    @Lob
    @Column(name = "descripción", length = 200)
    private String descripcion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "idIssue", referencedColumnName = "idIssue", nullable = false),
        @JoinColumn(name = "idReporte", referencedColumnName = "idReporte", nullable = false)
    })
    private Issue issue;
    
    // Constructores
    public Evidencia() {}
    
    public Evidencia(String nombre, byte[] evidencia, String descripcion, Issue issue) {
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
    
    public byte[] getEvidencia() {
        return evidencia;
    }
    
    public void setEvidencia(byte[] evidencia) {
        this.evidencia = evidencia;
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
