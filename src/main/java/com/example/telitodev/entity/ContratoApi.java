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

    public enum FormatoContrato {JSON, YAML}    //Limita opciones de formato
    @Enumerated(EnumType.STRING)
    @Column(name = "formato", nullable = false)
    private FormatoContrato  formato;

    @Column(name = "url_contrato", nullable = true, length = 45)
    private String urlContrato;

    @Column(name = "fecha_modificacion")
    private Timestamp fechaModificacion= new Timestamp(System.currentTimeMillis());

    @Column(name = "contenido", columnDefinition = "JSON")
    private String contenido;
    
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "idVersion", nullable = false, unique = true)
    private VersionApi versionApi;
    
    // Constructores
    public ContratoApi() {}
    
    public ContratoApi(FormatoContrato formato, String urlContrato, Timestamp fechaModificacion, VersionApi versionApi) {
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
    
    public FormatoContrato getFormato() {
        return formato;
    }
    
    public void setFormato(FormatoContrato formato) {
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
