package com.example.telitodev.dto;

import com.example.telitodev.entity.ContratoApi;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public class VersionContratoDTO {
    @NotNull(message = "No se pudo encontrar la API.")
    private Integer idApi;

    // Campos de VersionApi
    private Integer idVersion;

    @NotNull(message = "Coloque un número de versión")
    private String version;

    // Campos de ContratoApi
    private Integer idContrato;

    @NotNull(message = "Debe subir un contrato en formato JSON o YAML")
    private MultipartFile contrato; // si se sube archivo

    private ContratoApi.FormatoContrato formato;

    public enum MetodoCarga {archivo, texto, url}
    private MetodoCarga metodoCarga;

    private String contenido; // contenido del contrato si se pega
    private boolean desdeArchivo=true; // indica si se subió archivo o se pegó contenido


    public MetodoCarga getMetodoCarga() {
        return metodoCarga;
    }
    public void setMetodoCarga(MetodoCarga metodoCarga) {
        this.metodoCarga = metodoCarga;
    }

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

    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }

    public Integer getIdContrato() {
        return idContrato;
    }
    public void setIdContrato(Integer idContrato) {
        this.idContrato = idContrato;
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

    public Integer getIdApi() {
        return idApi;
    }
    public void setIdApi(Integer idApi) {
        this.idApi = idApi;
    }

    public MultipartFile getContrato() {
        return contrato;
    }
    public void setContrato(MultipartFile contrato) {
        this.contrato = contrato;
    }
}