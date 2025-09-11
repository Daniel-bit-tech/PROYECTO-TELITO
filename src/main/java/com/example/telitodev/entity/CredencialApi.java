package com.example.telitodev.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "credencialapi")
public class CredencialApi {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCredencialAPI")
    private Integer idCredencialApi;
    
    @Column(name = "api_key", nullable = false, length = 45)
    private String apiKey;
    
    @Column(name = "fecha_creacion", nullable = false)
    private Timestamp fechaCreacion;
    
    @Column(name = "estado", nullable = false)
    private Boolean estado;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuario", nullable = false, referencedColumnName = "dni")
    private Usuario usuario;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAPI", nullable = false)
    private Api api;
    
    // Constructores
    public CredencialApi() {}
    
    public CredencialApi(String apiKey, Timestamp fechaCreacion, Boolean estado,
                        Usuario usuario, Api api) {
        this.apiKey = apiKey;
        this.fechaCreacion = fechaCreacion;
        this.estado = estado;
        this.usuario = usuario;
        this.api = api;
    }
    
    // Getters y Setters
    public Integer getIdCredencialApi() {
        return idCredencialApi;
    }
    
    public void setIdCredencialApi(Integer idCredencialApi) {
        this.idCredencialApi = idCredencialApi;
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public Timestamp getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(Timestamp fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public Boolean getEstado() {
        return estado;
    }
    
    public void setEstado(Boolean estado) {
        this.estado = estado;
    }
    
    public Usuario getUsuario() {
        return usuario;
    }
    
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    
    public Api getApi() {
        return api;
    }
    
    public void setApi(Api api) {
        this.api = api;
    }
}
