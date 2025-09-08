package com.example.telitodev.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "metricaapi")
public class MetricaApi {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idMetrica")
    private Integer idMetrica;
    
    @Column(name = "fecha")
    private Timestamp fecha;
    
    @Column(name = "llamadas")
    private Integer llamadas;
    
    @Column(name = "errores")
    private Integer errores;
    
    @Column(name = "latencia_promedio")
    private Integer latenciaPromedio;
    
    @Column(name = "costo", precision = 10, scale = 2)
    private BigDecimal costo;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAPI", nullable = false)
    private Api api;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idEntorno", nullable = false)
    private Entorno entorno;
    
    // Constructores
    public MetricaApi() {}
    
    public MetricaApi(Timestamp fecha, Integer llamadas, Integer errores, 
                     Integer latenciaPromedio, BigDecimal costo, Api api, Entorno entorno) {
        this.fecha = fecha;
        this.llamadas = llamadas;
        this.errores = errores;
        this.latenciaPromedio = latenciaPromedio;
        this.costo = costo;
        this.api = api;
        this.entorno = entorno;
    }
    
    // Getters y Setters
    public Integer getIdMetrica() {
        return idMetrica;
    }
    
    public void setIdMetrica(Integer idMetrica) {
        this.idMetrica = idMetrica;
    }
    
    public Timestamp getFecha() {
        return fecha;
    }
    
    public void setFecha(Timestamp fecha) {
        this.fecha = fecha;
    }
    
    public Integer getLlamadas() {
        return llamadas;
    }
    
    public void setLlamadas(Integer llamadas) {
        this.llamadas = llamadas;
    }
    
    public Integer getErrores() {
        return errores;
    }
    
    public void setErrores(Integer errores) {
        this.errores = errores;
    }
    
    public Integer getLatenciaPromedio() {
        return latenciaPromedio;
    }
    
    public void setLatenciaPromedio(Integer latenciaPromedio) {
        this.latenciaPromedio = latenciaPromedio;
    }
    
    public BigDecimal getCosto() {
        return costo;
    }
    
    public void setCosto(BigDecimal costo) {
        this.costo = costo;
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
}
