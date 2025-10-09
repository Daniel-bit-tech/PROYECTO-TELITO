package com.example.telitodev.entity;

import jakarta.persistence.*;
import java.sql.Date;
import java.util.List;

@Entity
@Table(name = "proyecto")
public class Proyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idProyecto")
    private Integer idProyecto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idOrganizacion")
    private Organizacion organizacion;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="dni_po_lider")
    private Usuario usuarioLider;

    @Column(name = "nombre", length = 45, nullable = false)
    private String nombre;

    @Column(name = "descripcion", length = 100)
    private String descripcion;

    @Column(name = "fecha_inicio", nullable = false)
    private Date fechaInicio;

    @Column(name = "fecha_fin")
    private Date fechaFin;

    @Column(name = "url_repositorio")
    private String repositorio;

    @Column(name = "publico")
    private Boolean publico;

    @Column(name = "activo")
    private Boolean activo;

    //Relaciones
    @OneToMany(mappedBy = "proyecto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProyectoHasApi> proyectoHasApis;

    //Get y Set

    public Integer getIdProyecto() {
        return idProyecto;
    }
    public void setIdProyecto(Integer idProyecto) {
        this.idProyecto = idProyecto;
    }

    public Organizacion getOrganizacion() {
        return organizacion;
    }
    public void setOrganizacion(Organizacion organizacion) {
        this.organizacion = organizacion;
    }

    public Usuario getUsuarioLider() {
        return usuarioLider;
    }
    public void setUsuarioLider(Usuario usuarioLider) {
        this.usuarioLider = usuarioLider;
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

    public Date getFechaInicio() {
        return fechaInicio;
    }
    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Date getFechaFin() {
        return fechaFin;
    }
    public void setFechaFin(Date fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getRepositorio() {
        return repositorio;
    }
    public void setRepositorio(String repositorio) {
        this.repositorio = repositorio;
    }

    public Boolean getPublico() {
        return publico;
    }
    public void setPublico(Boolean publico) {
        this.publico = publico;
    }

    public Boolean getActivo() {
        return activo;
    }
    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public List<ProyectoHasApi> getProyectoHasApis() {
        return proyectoHasApis;
    }
    public void setProyectoHasApis(List<ProyectoHasApi> proyectoHasApis) {
        this.proyectoHasApis = proyectoHasApis;
    }
}
