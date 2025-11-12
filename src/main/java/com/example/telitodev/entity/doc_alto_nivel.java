package com.example.telitodev.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "doc_alto_nivel",
        uniqueConstraints = @UniqueConstraint(name = "uk_doc_alto_nivel_api", columnNames = "id_api"))
public class doc_alto_nivel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_doc")
    private Integer idDoc;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_api", nullable = false,
            foreignKey = @ForeignKey(name = "fk_doc_alto_nivel_api"))
    private Api api;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String beneficios;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String limitaciones;

    @Lob
    @Column(name = "flujo_funcional", columnDefinition = "LONGTEXT")
    private String flujoFuncional;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String sla;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String costos;

    // ✅ NUEVA COLUMNA AÑADIDA
    @Lob
    @Column(name = "ejemplos_integracion", columnDefinition = "LONGTEXT")
    private String ejemplosIntegracion;

    @Column(name = "creado_en", updatable = false)
    private Instant creadoEn;

    @Column(name = "actualizado_en")
    private Instant actualizadoEn;

    @PrePersist
    void onCreate() {
        creadoEn = actualizadoEn = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        actualizadoEn = Instant.now();
    }

    // Getters and Setters

    public Integer getIdDoc() {
        return idDoc;
    }

    public void setIdDoc(Integer idDoc) {
        this.idDoc = idDoc;
    }

    public Api getApi() {
        return api;
    }

    public void setApi(Api api) {
        this.api = api;
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

    // ✅ NUEVO GETTER Y SETTER
    public String getEjemplosIntegracion() {
        return ejemplosIntegracion;
    }

    public void setEjemplosIntegracion(String ejemplosIntegracion) {
        this.ejemplosIntegracion = ejemplosIntegracion;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(Instant creadoEn) {
        this.creadoEn = creadoEn;
    }

    public Instant getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(Instant actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }
}