package com.example.telitodev.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ejemploscodigo")
public class EjemplosCodigo {
    
    @Id
    @Column(name = "idEjemplosCodigo")
    private Integer idEjemplosCodigo;
    
    @Column(name = "lenguaje", length = 45)
    private String lenguaje;
    
    @Column(name = "contenido", columnDefinition = "JSON")
    private String contenido;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Documentacion_idDocumentacion", nullable = false)
    private Documentacion documentacion;
    
    // Constructores
    public EjemplosCodigo() {}
    
    public EjemplosCodigo(Integer idEjemplosCodigo, String lenguaje, String contenido, Documentacion documentacion) {
        this.idEjemplosCodigo = idEjemplosCodigo;
        this.lenguaje = lenguaje;
        this.contenido = contenido;
        this.documentacion = documentacion;
    }
    
    // Getters y Setters
    public Integer getIdEjemplosCodigo() {
        return idEjemplosCodigo;
    }
    
    public void setIdEjemplosCodigo(Integer idEjemplosCodigo) {
        this.idEjemplosCodigo = idEjemplosCodigo;
    }
    
    public String getLenguaje() {
        return lenguaje;
    }
    
    public void setLenguaje(String lenguaje) {
        this.lenguaje = lenguaje;
    }
    
    public String getContenido() {
        return contenido;
    }
    
    public void setContenido(String contenido) {
        this.contenido = contenido;
    }
    
    public Documentacion getDocumentacion() {
        return documentacion;
    }
    
    public void setDocumentacion(Documentacion documentacion) {
        this.documentacion = documentacion;
    }
}
