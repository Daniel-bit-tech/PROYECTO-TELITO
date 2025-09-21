package com.example.telitodev.entity;

import jakarta.persistence.*;

import java.sql.Date;

@Entity
@Table(name = "proyecto_has_api")
public class ProyectoHasApi {

    @EmbeddedId
    private ProyectoHasApiId proyectoHasApiId;

    @MapsId("idProyecto")
    @ManyToOne
    @JoinColumn(name = "idProyecto")
    private Proyecto proyecto;

    @MapsId("idApi")
    @ManyToOne
    @JoinColumn(name = "idAPI")
    private Api api;

    @Column(name = "fecha_asociacion")
    private Date fechaAsociacion;

    @Column(name = "proposito")
    private String proposito;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idVersion")
    private VersionApi version;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idEntorno")
    private Entorno entorno;

}
