package com.example.telitodev.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "documentacion")
public class Documentacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idDocumentacion")
    private Integer idDocumentacion;
    
    @Column(name = "tipo", length = 45)
    private String tipo;
    
    @Column(name = "contenido", columnDefinition = "TEXT")
    private String contenido;
    
    @Column(name = "fecha_creacion")
    private Timestamp fechaCreacion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAPI", nullable = false)
    private Api api;
    
    @OneToMany(mappedBy = "documentacion", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<EjemplosCodigo> ejemplosCodigo;
    
    // Constructores
    public Documentacion() {}
    
    public Documentacion(String tipo, String contenido, Timestamp fechaCreacion, Api api) {
        this.tipo = tipo;
        this.contenido = contenido;
        this.fechaCreacion = fechaCreacion;
        this.api = api;
    }
    
    // Getters y Setters
    public Integer getIdDocumentacion() {
        return idDocumentacion;
    }
    
    public void setIdDocumentacion(Integer idDocumentacion) {
        this.idDocumentacion = idDocumentacion;
    }
    
    public String getTipo() {
        return tipo;
    }
    
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    
    public String getContenido() {
        return contenido;
    }
    
    public void setContenido(String contenido) {
        this.contenido = contenido;
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
    
    public List<EjemplosCodigo> getEjemplosCodigo() {
        return ejemplosCodigo;
    }
    
    public void setEjemplosCodigo(List<EjemplosCodigo> ejemplosCodigo) {
        this.ejemplosCodigo = ejemplosCodigo;
    }
}
