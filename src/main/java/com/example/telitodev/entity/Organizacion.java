package com.example.telitodev.entity;

import jakarta.persistence.*;

import java.sql.Date;
import java.util.List;

@Entity
@Table(name = "organizacion")
public class Organizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idOrganizacion")
    private Integer idOrganizacion;

    @Column(name = "nombre", length = 45, nullable = false)
    private String nombre;

    @Column(name = "fecha_creacion", nullable = false)
    private Date fechaCreacion;

    @Column(name = "descripcion", length = 100)
    private String descripcion;

    //Relaciones
    @OneToMany(mappedBy = "organizacion",fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Usuario> usuarios;

    @OneToMany(mappedBy = "organizacion", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Equipo> equipos;

    // COMENTADO: Proyectos están relacionados con Equipo, no directamente con Organizacion
    // @OneToMany(mappedBy = "organizacion",fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    // private List<Proyecto> proyectos;

    //Get y Set


    public Integer getIdOrganizacion() {
        return idOrganizacion;
    }
    public void setIdOrganizacion(Integer idOrganizacion) {
        this.idOrganizacion = idOrganizacion;
    }

    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }
    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getDescripcion() {
        return descripcion;
    }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<Usuario> getUsuarios() {
        return usuarios;
    }
    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }

    public List<Equipo> getEquipos() {
        return equipos;
    }
    public void setEquipos(List<Equipo> equipos) {
        this.equipos = equipos;
    }

    // COMENTADO: Proyectos están relacionados con Equipo, no directamente con Organizacion
    // public List<Proyecto> getProyectos() {
    //     return proyectos;
    // }
    // public void setProyectos(List<Proyecto> proyectos) {
    //     this.proyectos = proyectos;
    // }
}
