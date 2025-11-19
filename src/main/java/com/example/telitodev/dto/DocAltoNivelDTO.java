package com.example.telitodev.dto;

import jakarta.validation.constraints.NotNull;

public class DocAltoNivelDTO {

    private Integer idDoc;

    private Integer idApi;

    @NotNull
    private String beneficios;

    @NotNull

    private String limitaciones;

    @NotNull
    private String flujoFuncional;

    @NotNull
    private String sla;

    @NotNull
    private String costos;

    @NotNull
    private String ejemplosIntegracion;


    public DocAltoNivelDTO() {
    }

    public DocAltoNivelDTO(String beneficios, Integer idApi, String limitaciones, String flujoFuncional, String sla, String costos, String ejemplosIntegracion) {
        this.beneficios = beneficios;
        this.idApi = idApi;
        this.limitaciones = limitaciones;
        this.flujoFuncional = flujoFuncional;
        this.sla = sla;
        this.costos = costos;
        this.ejemplosIntegracion = ejemplosIntegracion;
    }

    //Get and Set
    public Integer getIdDoc() {
        return idDoc;
    }
    public void setIdDoc(Integer idDoc) {
        this.idDoc = idDoc;
    }

    public Integer getIdApi() {
        return idApi;
    }
    public void setIdApi(Integer idApi) {
        this.idApi = idApi;
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

    public String getCostos() {
        return costos;
    }
    public void setCostos(String costos) {
        this.costos = costos;
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

    public String getEjemplosIntegracion() {
        return ejemplosIntegracion;
    }
    public void setEjemplosIntegracion(String ejemplosIntegracion) {
        this.ejemplosIntegracion = ejemplosIntegracion;
    }
}
