package com.example.telitodev.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
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

    public ProyectoHasApiId() {
    }

    public ProyectoHasApiId(Integer idProyecto, Integer idApi) {
        this.idProyecto = idProyecto;
        this.idApi = idApi;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProyectoHasApiId)) return false;
        ProyectoHasApiId that = (ProyectoHasApiId) o;
        return Objects.equals(idProyecto, that.idProyecto) &&
                Objects.equals(idApi, that.idApi);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idProyecto, idApi);
    }
}
