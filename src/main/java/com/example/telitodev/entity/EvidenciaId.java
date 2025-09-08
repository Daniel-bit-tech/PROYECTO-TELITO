package com.example.telitodev.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class EvidenciaId implements Serializable {
    
    @Column(name = "idEvidencia")
    private Integer idEvidencia;
    
    @Column(name = "idIssue", insertable = false, updatable = false)
    private Integer idIssue;
    
    @Column(name = "idReporte", insertable = false, updatable = false)
    private Integer idReporte;
    
    public EvidenciaId() {}
    
    public EvidenciaId(Integer idEvidencia, Integer idIssue, Integer idReporte) {
        this.idEvidencia = idEvidencia;
        this.idIssue = idIssue;
        this.idReporte = idReporte;
    }
    
    // Getters y Setters
    public Integer getIdEvidencia() {
        return idEvidencia;
    }
    
    public void setIdEvidencia(Integer idEvidencia) {
        this.idEvidencia = idEvidencia;
    }
    
    public Integer getIdIssue() {
        return idIssue;
    }
    
    public void setIdIssue(Integer idIssue) {
        this.idIssue = idIssue;
    }
    
    public Integer getIdReporte() {
        return idReporte;
    }
    
    public void setIdReporte(Integer idReporte) {
        this.idReporte = idReporte;
    }
    
    // equals y hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EvidenciaId that = (EvidenciaId) o;
        return Objects.equals(idEvidencia, that.idEvidencia) && 
               Objects.equals(idIssue, that.idIssue) &&
               Objects.equals(idReporte, that.idReporte);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(idEvidencia, idIssue, idReporte);
    }
}
