package com.example.telitodev.dto;

import com.example.telitodev.entity.Dominio;
import com.example.telitodev.entity.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ApiCreacionDTO {

    private Integer idApi;

    @NotBlank(message = "Su API debe tener un nombre")
    @Size(min = 5, max = 50, message = "El nombre debe tener entre 5 y 50 caracteres")
    private String nombre;

    @NotBlank(message = "Coloque una descripción a su API")
    @Size(min = 20, max = 150, message = "Describa su API con un texto entre 20 y 150 caracteres")
    private String descripcion;

    @NotBlank(message = "Proporcione una URL base para su API")
    @Pattern(regexp = "^(https?:\\/\\/)([A-Za-z0-9.-]+)(:\\d+)?(\\/.*)?$",
            message = "Proporcione una URL base válida para su API")
    private String endpointURL;

    @NotNull(message = "Asigne un dominio a su API para facilitar su búsqueda")
    private Integer idDominio;

    @NotNull(message = "Asigne un tag a su API para facilitar su búsqueda")
    private Integer idTag;


    public ApiCreacionDTO() {
    }

    public ApiCreacionDTO(Integer idApi, String descripcion, String endpointURL, Integer idDominio, Integer idTag) {
        this.idApi = idApi;
        this.descripcion = descripcion;
        this.endpointURL = endpointURL;
        this.idDominio = idDominio;
        this.idTag = idTag;
    }

    public ApiCreacionDTO(Integer idApi, String nombre, String descripcion, String endpointURL, Integer idDominio, Integer idTag) {
        this.idApi = idApi;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.endpointURL = endpointURL;
        this.idDominio = idDominio;
        this.idTag = idTag;
    }

    public Integer getIdApi() {
        return idApi;
    }
    public void setIdApi(Integer idApi) {
        this.idApi = idApi;
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

    public String getEndpointURL() {
        return endpointURL;
    }
    public void setEndpointURL(String endpointURL) {
        this.endpointURL = endpointURL;
    }

    public Integer getIdDominio() {
        return idDominio;
    }
    public void setIdDominio(Integer idDominio) {
        this.idDominio = idDominio;
    }

    public Integer getIdTag() {
        return idTag;
    }
    public void setIdTag(Integer idTag) {
        this.idTag = idTag;
    }
}
