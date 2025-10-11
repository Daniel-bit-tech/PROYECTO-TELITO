package com.example.telitodev.dto;

public class EnvironmentQADTO {
    private String nombre;
    private String urlBase;
    private String tipo;

    public EnvironmentQADTO() {
    }

    public EnvironmentQADTO(String nombre, String urlBase, String tipo) {
        this.nombre = nombre;
        this.urlBase = urlBase;
        this.tipo = tipo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUrlBase() {
        return urlBase;
    }

    public void setUrlBase(String urlBase) {
        this.urlBase = urlBase;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
