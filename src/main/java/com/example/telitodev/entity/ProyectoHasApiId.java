package com.example.telitodev.entity;

import java.io.Serializable;

public class ProyectoHasApiId implements Serializable {

    private Integer idProyecto;
    private Integer idApi;


    public Integer getIdProyecto() {
        return idProyecto;
    }
    public void setIdProyecto(Integer idProyecto) {
        this.idProyecto = idProyecto;
    }

    public Integer getIdApi() {
        return idApi;
    }
    public void setIdApi(Integer idApi) {
        this.idApi = idApi;
    }
}
