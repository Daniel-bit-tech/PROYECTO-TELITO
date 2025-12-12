package com.example.telitodev.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "proyecto")
public class Proyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idProyecto")
    private Integer idProyecto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idEquipo")
    private Equipo equipo;

    @Column(name = "dni_po_lider", length = 8, nullable = false)
    private String dniPoLider;

    @Size(min = 5, max = 45)
    @NotBlank
    @Column(name = "nombre", length = 45, nullable = false)
    private String nombre;

    @Size(min = 20, max = 100)
    @NotBlank
    @Column(name = "descripcion", length = 100)
    private String descripcion;

    @Column(name = "fecha_inicio", nullable = false)
    @PastOrPresent(message = "La fecha no puede ser futura")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    @Future(message = "La fecha de finalización debe ser futura")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaFin;

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

    public Equipo getEquipo() {
        return equipo;
    }
    public void setEquipo(Equipo equipo) {
        this.equipo = equipo;
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

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }
    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }
    public void setFechaFin(LocalDate fechaFin) {
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

    public String getDniPoLider() {
        return dniPoLider;
    }
    public void setDniPoLider(String dniPoLider) {
        this.dniPoLider = dniPoLider;
    }
}
