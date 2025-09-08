package com.example.telitodev.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "api_has_entorno")
public class ApiHasEntorno {
    
    @EmbeddedId
    private ApiHasEntornoId id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idApi")
    @JoinColumn(name = "idAPI")
    private Api api;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idEntorno")
    @JoinColumn(name = "idEntorno")
    private Entorno entorno;
    
    @Column(name = "url_base", length = 255)
    private String urlBase;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoApiEntorno estado;
    
    public enum EstadoApiEntorno {
        Activo, Inactivo
    }
    
    // Constructores
    public ApiHasEntorno() {}
    
    public ApiHasEntorno(Api api, Entorno entorno, String urlBase, EstadoApiEntorno estado) {
        this.id = new ApiHasEntornoId(api.getIdApi(), entorno.getIdEntorno());
        this.api = api;
        this.entorno = entorno;
        this.urlBase = urlBase;
        this.estado = estado;
    }
    
    // Getters y Setters
    public ApiHasEntornoId getId() {
        return id;
    }
    
    public void setId(ApiHasEntornoId id) {
        this.id = id;
    }
    
    public Api getApi() {
        return api;
    }
    
    public void setApi(Api api) {
        this.api = api;
    }
    
    public Entorno getEntorno() {
        return entorno;
    }
    
    public void setEntorno(Entorno entorno) {
        this.entorno = entorno;
    }
    
    public String getUrlBase() {
        return urlBase;
    }
    
    public void setUrlBase(String urlBase) {
        this.urlBase = urlBase;
    }
    
    public EstadoApiEntorno getEstado() {
        return estado;
    }
    
    public void setEstado(EstadoApiEntorno estado) {
        this.estado = estado;
    }
}
