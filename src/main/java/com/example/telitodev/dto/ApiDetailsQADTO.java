package com.example.telitodev.dto;

import java.util.List;
import java.util.Map;

public class ApiDetailsQADTO {
    private Integer id;
    private String nombre;
    private String descripcion;
    private String version;
    private Map<String, Object> spec;
    private List<EnvironmentQADTO> entornos;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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
}
