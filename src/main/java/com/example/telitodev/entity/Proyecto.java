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
    private Boolean Publico;

    @OneToMany(mappedBy = "proyecto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProyectoHasApi> proyectoHasApis;

}
