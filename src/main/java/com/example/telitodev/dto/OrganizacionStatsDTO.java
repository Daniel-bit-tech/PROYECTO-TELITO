package com.example.telitodev.dto;

import java.util.Date;

public class OrganizacionStatsDTO {
    private String nombre;
    private String descripcion;
    private Date fechaCreacion;
    private Long proyectosActivos;
    private Long apisAsociadas;
    private Long miembros;

    // Constructor
    public OrganizacionStatsDTO(String nombre, String descripcion, Date fechaCreacion,
                                Long proyectosActivos, Long apisAsociadas, Long miembros) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaCreacion = fechaCreacion;
        this.proyectosActivos = proyectosActivos;
        this.apisAsociadas = apisAsociadas;
        this.miembros = miembros;
    }

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Long getProyectosActivos() { return proyectosActivos; }
    public void setProyectosActivos(Long proyectosActivos) { this.proyectosActivos = proyectosActivos; }

    public Long getApisAsociadas() { return apisAsociadas; }
    public void setApisAsociadas(Long apisAsociadas) { this.apisAsociadas = apisAsociadas; }

    public Long getMiembros() { return miembros; }
    public void setMiembros(Long miembros) { this.miembros = miembros; }
}