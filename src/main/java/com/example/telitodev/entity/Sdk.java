package com.example.telitodev.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "sdk")
public class Sdk {
    
    @Id
    @Column(name = "idSDK")
    private Integer idSdk;
    
    @Column(name = "url_sdk", length = 200)
    private String urlSdk;
    
    @Column(name = "versionSDK", length = 45)
    private String versionSdk;
    
    @Column(name = "descripcion", nullable = false, length = 45)
    private String descripcion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAPI", nullable = false)
    private Api api;
    
    // Constructores
    public Sdk() {}
    
    public Sdk(Integer idSdk, String urlSdk, String versionSdk, String descripcion, Api api) {
        this.idSdk = idSdk;
        this.urlSdk = urlSdk;
        this.versionSdk = versionSdk;
        this.descripcion = descripcion;
        this.api = api;
    }
    
    // Getters y Setters
    public Integer getIdSdk() {
        return idSdk;
    }
    
    public void setIdSdk(Integer idSdk) {
        this.idSdk = idSdk;
    }
    
    public String getUrlSdk() {
        return urlSdk;
    }
    
    public void setUrlSdk(String urlSdk) {
        this.urlSdk = urlSdk;
    }
    
    public String getVersionSdk() {
        return versionSdk;
    }
    
    public void setVersionSdk(String versionSdk) {
        this.versionSdk = versionSdk;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public Api getApi() {
        return api;
    }
    
    public void setApi(Api api) {
        this.api = api;
    }
}
