package com.example.telitodev.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ApiHasEntornoId implements Serializable {
    
    private Integer idApi;
    private Integer idEntorno;
    
    public ApiHasEntornoId() {}
    
    public ApiHasEntornoId(Integer idApi, Integer idEntorno) {
        this.idApi = idApi;
        this.idEntorno = idEntorno;
    }
    
    // Getters y Setters
    public Integer getIdApi() {
        return idApi;
    }
    
    public void setIdApi(Integer idApi) {
        this.idApi = idApi;
    }
    
    public Integer getIdEntorno() {
        return idEntorno;
    }
    
    public void setIdEntorno(Integer idEntorno) {
        this.idEntorno = idEntorno;
    }
    
    // equals y hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiHasEntornoId that = (ApiHasEntornoId) o;
        return Objects.equals(idApi, that.idApi) && 
               Objects.equals(idEntorno, that.idEntorno);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(idApi, idEntorno);
    }
}
