package com.example.telitodev.dto;

import java.util.List;
import java.util.Map;

public class SandboxApiDetailsDto {
    private String nombre;
    private List<SandboxEnvironmentDto> entornos;
    private Map<String, Object> spec; // Para el JSON de OpenAPI


    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public List<SandboxEnvironmentDto> getEntornos() { return entornos; }
    public void setEntornos(List<SandboxEnvironmentDto> entornos) { this.entornos = entornos; }
    public Map<String, Object> getSpec() { return spec; }
    public void setSpec(Map<String, Object> spec) { this.spec = spec; }
}