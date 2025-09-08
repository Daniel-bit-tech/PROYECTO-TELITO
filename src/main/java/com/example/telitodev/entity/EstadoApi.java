package com.example.telitodev.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "estadoapi")
public class EstadoApi {
    
    @Id
    @Column(name = "idEstado")
    private Integer idEstado;
    
    @Column(name = "estado", nullable = false, length = 25)
    private String estado;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAPI", nullable = false)
    private Api api;
    
    // Constructores
    public EstadoApi() {}
    
    public EstadoApi(Integer idEstado, String estado, Api api) {
        this.idEstado = idEstado;
        this.estado = estado;
        this.api = api;
    }
    
    // Getters y Setters
    public Integer getIdEstado() {
        return idEstado;
    }
    
    public void setIdEstado(Integer idEstado) {
        this.idEstado = idEstado;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public Api getApi() {
        return api;
    }
    
    public void setApi(Api api) {
        this.api = api;
    }
}
