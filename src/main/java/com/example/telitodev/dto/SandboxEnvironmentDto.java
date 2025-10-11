package com.example.telitodev.dto;

public class SandboxEnvironmentDto {
    private String nombre;
    private String url_base;


    public SandboxEnvironmentDto(String nombre, String url_base) {
        this.nombre = nombre;
        this.url_base = url_base;
    }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getUrl_base() { return url_base; }
    public void setUrl_base(String url_base) { this.url_base = url_base; }
}