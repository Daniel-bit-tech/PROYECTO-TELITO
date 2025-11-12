package com.example.telitodev.dto;

import com.example.telitodev.entity.Documentacion;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.util.List;

public class DocGeneralDTO {

    // Campos comunes
    private Integer idApi;
    private String nombreApi;
    private Integer idVersion;
    private String numeroVersion;


    // Campos para documentación de alto nivel (tabla: doc_alto_nivel)
    private Integer idDocAltoNivel;
    @Size(min = 100, max = 400,message = "Describa este campo con entre 100 y 400 caracteres.")
    private String beneficios;
    @Size(min = 100, max = 400,message = "Describa este campo con entre 100 y 400 caracteres.")
    private String limitaciones;
    @Size(min = 100, max = 400,message = "Describa este campo con entre 100 y 400 caracteres.")
    private String flujoFuncional;
    @Size(min = 100, max = 400,message = "Describa este campo con entre 100 y 400 caracteres.")
    private String sla;
    @Size(min = 100, max = 400,message = "Describa este campo con entre 100 y 400 caracteres.")
    private String costos;
    @Size(min = 100, max = 400,message = "Describa este campo con entre 100 y 400 caracteres.")
    private String ejemplosIntegracion;

    // Campos auxiliares para la vista
//    private List<MultipartFile> archivosTecnicos;
//    private List<String> descripcionesTecnicas;
    private MultipartFile[] archivosTecnicos;
    private String[] descripcionesTecnicas;
    private String[] formatosTecnicos;

    // Getters y Setters
    public Integer getIdApi() {
        return idApi;
    }
    public void setIdApi(Integer idApi) {
        this.idApi = idApi;
    }

    public String getNombreApi() {
        return nombreApi;
    }
    public void setNombreApi(String nombreApi) {
        this.nombreApi = nombreApi;
    }

    public String getNumeroVersion() {
        return numeroVersion;
    }
    public void setNumeroVersion(String numeroVersion) {
        this.numeroVersion = numeroVersion;
    }

    public Integer getIdVersion() {
        return idVersion;
    }
    public void setIdVersion(Integer idVersion) {
        this.idVersion = idVersion;
    }

    public Integer getIdDocAltoNivel() {
        return idDocAltoNivel;
    }
    public void setIdDocAltoNivel(Integer idDocAltoNivel) {
        this.idDocAltoNivel = idDocAltoNivel;
    }

    public String getBeneficios() {
        return beneficios;
    }
    public void setBeneficios(String beneficios) {
        this.beneficios = beneficios;
    }

    public String getLimitaciones() {
        return limitaciones;
    }
    public void setLimitaciones(String limitaciones) {
        this.limitaciones = limitaciones;
    }

    public String getFlujoFuncional() {
        return flujoFuncional;
    }
    public void setFlujoFuncional(String flujoFuncional) {
        this.flujoFuncional = flujoFuncional;
    }

    public String getSla() {
        return sla;
    }
    public void setSla(String sla) {
        this.sla = sla;
    }

    public String getCostos() {
        return costos;
    }
    public void setCostos(String costos) {
        this.costos = costos;
    }

    public String getEjemplosIntegracion() {
        return ejemplosIntegracion;
    }
    public void setEjemplosIntegracion(String ejemplosIntegracion) {
        this.ejemplosIntegracion = ejemplosIntegracion;
    }

//    public List<MultipartFile> getArchivosTecnicos() {
//        return archivosTecnicos;
//    }
//    public void setArchivosTecnicos(List<MultipartFile> archivosTecnicos) {
//        this.archivosTecnicos = archivosTecnicos;
//    }
//
//    public List<String> getDescripcionesTecnicas() {
//        return descripcionesTecnicas;
//    }
//    public void setDescripcionesTecnicas(List<String> descripcionesTecnicas) {
//        this.descripcionesTecnicas = descripcionesTecnicas;
//    }
    public MultipartFile[] getArchivosTecnicos() {
        return archivosTecnicos;
    }
    public void setArchivosTecnicos(MultipartFile[] archivosTecnicos) {
        this.archivosTecnicos = archivosTecnicos;
    }

    public String[] getDescripcionesTecnicas() {
        return descripcionesTecnicas;
    }
    public void setDescripcionesTecnicas(String[] descripcionesTecnicas) {
        this.descripcionesTecnicas = descripcionesTecnicas;
    }

    public String[] getFormatosTecnicos() {
        return formatosTecnicos;
    }
    public void setFormatosTecnicos(String[] formatosTecnicos) {
        this.formatosTecnicos = formatosTecnicos;
    }
}
