package com.example.telitodev.entity;


import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "dominio")
public class Dominio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idDominio")
    private Integer idDominio;

    @Column(name = "nombre", length = 20)
    private String nombre;

    //Relaciones
    @OneToMany(mappedBy = "dominio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Api> apisDominio;

    //Get y Set


    public Integer getIdDominio() {
        return idDominio;
    }
    public void setIdDominio(Integer idDominio) {
        this.idDominio = idDominio;
    }

    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Api> getApisDominio() {
        return apisDominio;
    }
    public void setApisDominio(List<Api> apisDominio) {
        this.apisDominio = apisDominio;
    }
}
