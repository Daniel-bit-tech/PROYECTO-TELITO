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
    private Integer formato;
    
    @Column(name = "url_documento", nullable = false, length = 45)
    private String urlDocumento;
    
    @Column(name = "fecha_subida")
    private Timestamp fechaSubida;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAPI", nullable = false)
    private Api api;
    
    // Constructores
    public ContratoApi() {}
    
    public ContratoApi(Integer formato, String urlDocumento, Timestamp fechaSubida, Api api) {
        this.formato = formato;
        this.urlDocumento = urlDocumento;
        this.fechaSubida = fechaSubida;
        this.api = api;
    }
    
    // Getters y Setters
    public Integer getIdContratoApi() {
        return idContratoApi;
    }
    
    public void setIdContratoApi(Integer idContratoApi) {
        this.idContratoApi = idContratoApi;
    }
    
    public Integer getFormato() {
        return formato;
    }
    
    public void setFormato(Integer formato) {
        this.formato = formato;
    }
    
    public String getUrlDocumento() {
        return urlDocumento;
    }
    
    public void setUrlDocumento(String urlDocumento) {
        this.urlDocumento = urlDocumento;
    }
    
    public Timestamp getFechaSubida() {
        return fechaSubida;
    }
    
    public void setFechaSubida(Timestamp fechaSubida) {
        this.fechaSubida = fechaSubida;
    }
    
    public Api getApi() {
        return api;
    }
    
    public void setApi(Api api) {
        this.api = api;
    }
}
