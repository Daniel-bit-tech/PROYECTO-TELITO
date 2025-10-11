package com.example.telitodev.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiQADTO {
    private Integer id;
    private String nombre;
    private String descripcion;
    private String version;
    private Map<String, Object> spec;
    private List<EnvironmentQADTO> entornos;
    private Boolean activo;

    // Constructores
    public ApiQADTO() {}

    public ApiQADTO(Integer id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.version = "1.0.0";
        this.activo = true;
        this.spec = new HashMap<>();
        this.entornos = new ArrayList<>();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, Object> getSpec() {
        return spec;
    }

    public void setSpec(Map<String, Object> spec) {
        this.spec = spec;
    }

    public List<EnvironmentQADTO> getEntornos() {
        return entornos;
    }

    public void setEntornos(List<EnvironmentQADTO> entornos) {
        this.entornos = entornos;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}