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

    @OneToMany(mappedBy = "api", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Api> apisDominio;

}
