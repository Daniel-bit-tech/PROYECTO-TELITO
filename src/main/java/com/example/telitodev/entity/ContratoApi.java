package com.example.telitodev.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "contratoapi")
public class ContratoApi {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idContratoAPI")
    private Integer idContratoApi;
    
    @Column(name = "formato", nullable = false)
    private String formato;

    @Column(name = "url_contrato", nullable = true, length = 45)
    private String urlContrato;

    @Column(name = "fecha_modificacion")
    private Timestamp fechaModificacion;

    @Column(name = "contenido", columnDefinition = "JSON")
    private String contenido;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idVersion", nullable = false)
    private VersionApi versionApi;
    
    // Constructores
    public ContratoApi() {}
    
    public ContratoApi(String formato, String urlContrato, Timestamp fechaModificacion, VersionApi versionApi) {
        this.formato = formato;
        this.urlContrato = urlContrato;
        this.fechaModificacion = fechaModificacion;
        this.versionApi = versionApi;
    }
    
    // Getters y Setters
    public Integer getIdContratoApi() {
        return idContratoApi;
    }
    
    public void setIdContratoApi(Integer idContratoApi) {
        this.idContratoApi = idContratoApi;
    }
    
    public String getFormato() {
        return formato;
    }
    
    public void setFormato(String formato) {
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

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public VersionApi getVersionApi() {
        return versionApi;
    }

    public void setVersionApi(VersionApi versionApi) {
        this.versionApi = versionApi;
    }
}
