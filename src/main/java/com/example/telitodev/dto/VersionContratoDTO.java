package com.example.telitodev.dto;

import com.example.telitodev.entity.ContratoApi;
import com.example.telitodev.entity.VersionApi;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class VersionContratoDTO {
    // Campos de VersionApi
    private Integer idVersion=0;

    @NotNull(message = "Coloque un número de versión")
    private String version;

    @NotNull(message = "Elija un estado de su nueva versión")
    private VersionApi.EstadoVersion estadoVersion;

    @FutureOrPresent(message = "La fecha de publicación debe ser futura")
    private LocalDate fechaPublicacion;
    private Integer idApi;
    private String nombreApi;

    // Campos de ContratoApi
    private Integer idContrato=0;

    @NotNull(message = "Debe elegir un formato")
    private ContratoApi.FormatoContrato formato;

    private String contenido; // contenido del contrato si se pega
    private String urlContrato;
    private Timestamp fechaModificacion;
    private MultipartFile archivo; // si se sube archivo
    private boolean desdeArchivo=true; // indica si se subió archivo o se pegó contenido





    public boolean isDesdeArchivo() {
        return desdeArchivo;
    }
    public void setDesdeArchivo(boolean desdeArchivo) {
        this.desdeArchivo = desdeArchivo;
    }

    public Integer getIdVersion() {
        return idVersion;
    }
    public void setIdVersion(Integer idVersion) {
        this.idVersion = idVersion;
    }

    public LocalDate getFechaPublicacion() {
        return fechaPublicacion;
    }
    public void setFechaPublicacion(LocalDate fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public VersionApi.EstadoVersion getEstadoVersion() {
        return estadoVersion;
    }
    public void setEstadoVersion(VersionApi.EstadoVersion estadoVersion) {
        this.estadoVersion = estadoVersion;
    }

    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }

    public String getNombreAPI() {
        return nombreApi;
    }
    public void setNombreAPI(String nombreAPI) {
        this.nombreApi = nombreAPI;
    }

    public Integer getIdContrato() {
        return idContrato;
    }
    public void setIdContrato(Integer idContrato) {
        this.idContrato = idContrato;
    }

    public Integer getIdAPI() {
        return idApi;
    }
    public void setIdAPI(Integer idAPI) {
        this.idApi = idAPI;
    }

    public String getContenido() {
        return contenido;
    }
    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public ContratoApi.FormatoContrato getFormato() {
        return formato;
    }
    public void setFormato(ContratoApi.FormatoContrato formato) {
        this.formato = formato;
    }

    public String getUrlContrato() {
        return urlContrato;
    }
    public void setUrlContrato(String urlContrato) {
        this.urlContrato = urlContrato;
    }

    public Timestamp getFechaModificacion() {
        return fechaModificacion;
    }
    public void setFechaModificacion(Timestamp fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public MultipartFile getArchivo() {
        return archivo;
    }
    public void setArchivo(MultipartFile archivo) {
        this.archivo = archivo;
    }
}