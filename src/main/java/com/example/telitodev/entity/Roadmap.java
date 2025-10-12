package com.example.telitodev.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "roadmap")
public class Roadmap {

    public enum EstadoRoadmap {
        Nueva("Nueva"),
        En_desarrollo("En desarrollo"),
        Próxima("Próxima");

        private final String displayName;

        EstadoRoadmap(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return name(); // Devuelve el nombre del enum (En_desarrollo)
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_id", referencedColumnName = "idAPI", nullable = false)
    private Api api;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, columnDefinition = "ENUM('Nueva','En_desarrollo','Próxima')")
    private EstadoRoadmap estado;

    @Column(name = "fecha_modificacion", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaModificacion;


    // Constructores
    public Roadmap() {}

    public Roadmap(Api api, EstadoRoadmap estado, Date fechaInicio, Date fechaFin) {
        this.api = api;
        this.estado = estado;
        this.fechaModificacion = fechaInicio;
    }

    @Override
    public String toString() {
        return "Roadmap{" +
                "id=" + id +
                ", api='" + api + '\'' +
                ", estado='" + estado + '\'' +
                ", fechaModificacion=" + fechaModificacion +

                '}';
    }

    public Roadmap(String estado, Api api) {
        this.estado = EstadoRoadmap.valueOf(estado);
        this.api = api;
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

    public EstadoRoadmap getEstado() {
        return estado;
    }

    public void setEstado(EstadoRoadmap estado) {
        this.estado = estado;
    }

    public Date getFechaModificacion() {
        return fechaModificacion;
    }
    public void setFechaModificacion(Date fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public String getEstadoString() {
        return estado.getDisplayName();
    }

    public void setEstadoString(String estado) {
        this.estado = EstadoRoadmap.valueOf(estado);
    }


}