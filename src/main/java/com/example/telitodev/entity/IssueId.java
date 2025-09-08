package com.example.telitodev.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class IssueId implements Serializable {
    
    private Integer idIssue;
    private Integer idReporte;
    
    public IssueId() {}
    
    public IssueId(Integer idIssue, Integer idReporte) {
        this.idIssue = idIssue;
        this.idReporte = idReporte;
    }
    
    // Getters y Setters
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
        IssueId issueId = (IssueId) o;
        return Objects.equals(idIssue, issueId.idIssue) && 
               Objects.equals(idReporte, issueId.idReporte);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(idIssue, idReporte);
    }
}
