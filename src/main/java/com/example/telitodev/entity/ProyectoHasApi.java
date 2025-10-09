package com.example.telitodev.entity;

import jakarta.persistence.*;

import java.sql.Date;

@Entity
@Table(name = "proyecto_has_api")
public class ProyectoHasApi {

    @EmbeddedId
    private ProyectoHasApiId proyectoHasApiId = new ProyectoHasApiId();

    @MapsId("idProyecto")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idProyecto")
    private Proyecto proyecto;

    @MapsId("idApi")
    @ManyToOne(fetch = FetchType.LAZY)
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


    public ProyectoHasApi() {}

    public ProyectoHasApi(Proyecto proyecto, Api api, Date fechaAsociacion, String proposito, VersionApi version, Entorno entorno) {
        this.proyecto = proyecto;
        this.api = api;
        this.fechaAsociacion = fechaAsociacion;
        this.proposito = proposito;
        this.version = version;
        this.entorno = entorno;
    }


    //Get y Set

    public ProyectoHasApiId getProyectoHasApiId() {
        return proyectoHasApiId;
    }
    public void setProyectoHasApiId(ProyectoHasApiId proyectoHasApiId) {
        this.proyectoHasApiId = proyectoHasApiId;
    }

    public Proyecto getProyecto() {
        return proyecto;
    }
    public void setProyecto(Proyecto proyecto) {
        this.proyecto = proyecto;
    }

    public Api getApi() {
        return api;
    }
    public void setApi(Api api) {
        this.api = api;
    }

    public Date getFechaAsociacion() {
        return fechaAsociacion;
    }
    public void setFechaAsociacion(Date fechaAsociacion) {
        this.fechaAsociacion = fechaAsociacion;
    }

    public String getProposito() {
        return proposito;
    }
    public void setProposito(String proposito) {
        this.proposito = proposito;
    }

    public VersionApi getVersion() {
        return version;
    }
    public void setVersion(VersionApi version) {
        this.version = version;
    }

    public Entorno getEntorno() {
        return entorno;
    }
    public void setEntorno(Entorno entorno) {
        this.entorno = entorno;
    }
}
