package com.example.telitodev.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "estadoapi")
public class EstadoApi {

    @Id
    @Column(name = "idEstado")
    private Integer idEstado;

    @Column(name = "estado", nullable = false, length = 25)
    private String estado;


    // Relaciones
    @OneToMany(mappedBy = "estadoApi")
    private List<Api> apis;

    // Constructores
    public EstadoApi() {}

    public EstadoApi(Integer idEstado, String estado) {
        this.idEstado = idEstado;
        this.estado = estado;
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

    public List<Api> getApis() {
        return apis;
    }

    public void setApis(List<Api> apis) {
        this.apis = apis;
    }
}