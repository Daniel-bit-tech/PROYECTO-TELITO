package com.example.telitodev.dto;

import com.example.telitodev.entity.Dominio;
import com.example.telitodev.entity.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ApiCreacionDTO {

    @NotBlank(message = "Su API debe tener un nombre")
    @Size(min = 5, max = 50, message = "El nombre debe tener menos de 50 caracteres")
    private String nombre;

    @NotBlank(message = "Coloque una descripción a su API")
    @Size(min = 20, max = 150, message = "Describa su API con un texto entre 20 y 150 caracteres")
    private String descripcion;

    @NotBlank(message = "Proporcione una URL base para su API")
    @Pattern(regexp = "^(https?://)([\\w.-]+)(:\\d+)?(/)?$",
            message = "Proporcione una URL base válida para su API")
    private String endpointURL;

    @NotNull(message = "Asigne un dominio a su API para facilitar su búsqueda")
    private Dominio dominio;

    @NotNull(message = "Asigne un tag a su API para facilitar su búsqueda")
    private Tag tag;


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

    public String getEndpointURL() {
        return endpointURL;
    }
    public void setEndpointURL(String endpointURL) {
        this.endpointURL = endpointURL;
    }

    public Dominio getDominio() {
        return dominio;
    }
    public void setDominio(Dominio dominio) {
        this.dominio = dominio;
    }

    public Tag getTag() {
        return tag;
    }
    public void setTag(Tag tag) {
        this.tag = tag;
    }
}
