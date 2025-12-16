package com.example.telitodev.entity;

import jakarta.persistence.*;
import java.sql.Date;
//import java.util.Date;
import java.util.List;

@Entity
@Table(name = "equipo")
public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEquipo")
    private Integer idEquipo;

    @Column(name = "nombre", length = 45, nullable = false)
    @Column(name = "nombre", nullable = false, length = 45)
    private String nombre;

    @Column(name = "fecha_creacion", nullable = false)
    //@Temporal(TemporalType.DATE)
    private Date fechaCreacion;

    @Lob
    @Column(name = "logo", columnDefinition = "LONGBLOB")
    private byte[] logo;

    @ManyToOne
    @JoinColumn(name = "idOrganizacion", nullable = false)
    private Organizacion organizacion;

    //Relaciones
    @OneToMany(mappedBy = "equipo",fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Usuario> usuarios;

    @OneToMany(mappedBy = "equipo", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Proyecto> proyectos;

    @OneToMany(mappedBy = "equipo", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Api> apis;


    // Constructores, getters y setters
    public Equipo() {}

    public Equipo(String nombre, Date fechaCreacion, Organizacion organizacion) {
        this.nombre = nombre;
        this.fechaCreacion = fechaCreacion;
        this.organizacion = organizacion;
    }


    //Getters y Setters
    public Integer getIdEquipo() {
        return idEquipo;
    }
    public void setIdEquipo(Integer idEquipo) {
        this.idEquipo = idEquipo;
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

    public byte[] getLogo() {
        return logo;
    }
    public void setLogo(byte[] logo) {
        this.logo = logo;
    }

    public Organizacion getOrganizacion() {
        return organizacion;
    }
    public void setOrganizacion(Organizacion organizacion) {
        this.organizacion = organizacion;
    }

    public List<Api> getApis() {
        return apis;
    }
    public void setApis(List<Api> apis) {
        this.apis = apis;
    }

    public List<Usuario> getUsuarios() {
        return usuarios;
    }
    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }

    public List<Proyecto> getProyectos() {
        return proyectos;
    }
    public void setProyectos(List<Proyecto> proyectos) {
        this.proyectos = proyectos;
    }
}
