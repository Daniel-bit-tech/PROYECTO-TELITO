package com.example.telitodev.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "versionapi")
public class VersionApi {
    
    @Id
    @Column(name = "idVersion")
    private Integer idVersion;
    
    @Column(name = "version", length = 45)
    private String version;
    
    @Column(name = "estado_version", length = 45)
    private String estadoVersion;
    
    @Column(name = "fecha_publicacion")
    private Timestamp fechaPublicacion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAPI", nullable = false)
    private Api api;
    
    // Constructores
    public VersionApi() {}
    
    public VersionApi(Integer idVersion, String version, String estadoVersion, 
                     Timestamp fechaPublicacion, Api api) {
        this.idVersion = idVersion;
        this.version = version;
        this.estadoVersion = estadoVersion;
        this.fechaPublicacion = fechaPublicacion;
        this.api = api;
    }
    
    // Getters y Setters
    public Integer getIdVersion() {
        return idVersion;
    }
    
    public void setIdVersion(Integer idVersion) {
        this.idVersion = idVersion;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getEstadoVersion() {
        return estadoVersion;
    }
    
    public void setEstadoVersion(String estadoVersion) {
        this.estadoVersion = estadoVersion;
    }
    
    public Timestamp getFechaPublicacion() {
        return fechaPublicacion;
    }
    
    public void setFechaPublicacion(Timestamp fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }
    
    public Api getApi() {
        return api;
    }
    
    public void setApi(Api api) {
        this.api = api;
    }
}
