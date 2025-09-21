package com.example.telitodev.entity;

import jakarta.persistence.*;

import java.util.List;


@Entity
@Table(name = "tag")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTag")
    private Integer idTag;

    @Column(name = "nombre", length = 20)
    private String nombre;

    @OneToMany(mappedBy = "api", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Api> apisTag;

}
