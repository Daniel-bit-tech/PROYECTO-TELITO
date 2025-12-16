package com.example.telitodev.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "roadmap")
public class Roadmap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_id", referencedColumnName = "idAPI", nullable = false, unique = true)
    private Api api;

    @Column(name = "estado", nullable = false, length = 50)
    private String estado;  // Nueva, En desarrollo, Próxima, Sin estado

    @Column(name = "fecha_modificacion", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaModificacion;

    // Constructores
    public Roadmap() {}

    // Constructor corregido
    public Roadmap(Api api, String estado, Date fechaModificacion) {
        this.api = api;
        this.estado = estado;
        this.fechaModificacion = fechaModificacion;
    }

    // Constructor simplificado con fecha automática
    public Roadmap(Api api, String estado) {
        this.api = api;
        this.estado = estado;
        this.fechaModificacion = new Date();
    }

    @Override
    public String toString() {
        return "Roadmap{" +
                "id=" + id +
                ", api='" + (api != null ? api.getNombre() : "null") + '\'' +
                ", estado='" + estado + '\'' +
                ", fechaModificacion=" + fechaModificacion +
                '}';
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Api getApi() {
        return api;
    }

    public void setApi(Api api) {
        this.api = api;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
        // Actualizar automáticamente la fecha cuando cambia el estado
        this.fechaModificacion = new Date();
    }

    public Date getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(Date fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }
}