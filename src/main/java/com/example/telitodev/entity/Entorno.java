package com.example.telitodev.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "entorno")
public class Entorno {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEntorno")
    private Integer idEntorno;
    
    @Column(name = "nombre", length = 45)
    private String nombre;
    
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;
    
    @OneToMany(mappedBy = "entorno", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ApiHasEntorno> apiHasEntornos;
    
    @OneToMany(mappedBy = "entorno", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MetricaApi> metricasApi;
    
    // Constructores
    public Entorno() {}
    
    public Entorno(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }
    
    // Getters y Setters
    public Integer getIdEntorno() {
        return idEntorno;
    }
    
    public void setIdEntorno(Integer idEntorno) {
        this.idEntorno = idEntorno;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public List<ApiHasEntorno> getApiHasEntornos() {
        return apiHasEntornos;
    }
    
    public void setApiHasEntornos(List<ApiHasEntorno> apiHasEntornos) {
        this.apiHasEntornos = apiHasEntornos;
    }
    
    public List<MetricaApi> getMetricasApi() {
        return metricasApi;
    }
    
    public void setMetricasApi(List<MetricaApi> metricasApi) {
        this.metricasApi = metricasApi;
    }
}
