package com.example.telitodev.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "logapi")
public class LogApi {
    
    @Id
    @Column(name = "idLogAPI")
    private Integer idLogApi;
    
    @Column(name = "endpoint", nullable = false, length = 200)
    private String endpoint;
    
    @Column(name = "estadohttp", nullable = false)
    private Integer estadoHttp;
    
    @Column(name = "tiempoRespuestams", nullable = false)
    private Integer tiempoRespuestaMs;
    
    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAPI", nullable = false)
    private Api api;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuario", nullable = false)
    private Usuario usuario;
    
    // Constructores
    public LogApi() {}
    
    public LogApi(Integer idLogApi, String endpoint, Integer estadoHttp, 
                 Integer tiempoRespuestaMs, LocalDateTime fecha, Api api, Usuario usuario) {
        this.idLogApi = idLogApi;
        this.endpoint = endpoint;
        this.estadoHttp = estadoHttp;
        this.tiempoRespuestaMs = tiempoRespuestaMs;
        this.fecha = fecha;
        this.api = api;
        this.usuario = usuario;
    }
    
    // Getters y Setters
    public Integer getIdLogApi() {
        return idLogApi;
    }
    
    public void setIdLogApi(Integer idLogApi) {
        this.idLogApi = idLogApi;
    }
    
    public String getEndpoint() {
        return endpoint;
    }
    
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
    
    public Integer getEstadoHttp() {
        return estadoHttp;
    }
    
    public void setEstadoHttp(Integer estadoHttp) {
        this.estadoHttp = estadoHttp;
    }
    
    public Integer getTiempoRespuestaMs() {
        return tiempoRespuestaMs;
    }
    
    public void setTiempoRespuestaMs(Integer tiempoRespuestaMs) {
        this.tiempoRespuestaMs = tiempoRespuestaMs;
    }
    
    public LocalDateTime getFecha() {
        return fecha;
    }
    
    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
    
    public Api getApi() {
        return api;
    }
    
    public void setApi(Api api) {
        this.api = api;
    }
    
    public Usuario getUsuario() {
        return usuario;
    }
    
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
