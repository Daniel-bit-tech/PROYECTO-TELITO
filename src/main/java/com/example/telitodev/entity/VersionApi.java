package com.example.telitodev.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "versionapi")
public class VersionApi {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idVersion")
    private Integer idVersion;
    
    @Column(name = "version", length = 45)
    private String version;

    public enum EstadoVersion {     //Limita opciones de estado de Version
        ESTABLE("Estable"),
        EN_PRUEBAS("En Pruebas"),
        EN_CONSTRUCCION("En Construcción"),
        DEPRECADA("Deprecada"),
        RETIRADA("Retirada");

        private final String label;
        EstadoVersion(String label) {
            this.label = label;
        }
        public String getLabel() {
            return label;
        }
    }
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_version", length = 45)
    private EstadoVersion estadoVersion;
    
    @Column(name = "fecha_publicacion")
    private LocalDate fechaPublicacion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "idAPI", nullable = false)
    private Api api;


    //Relaciones
    @OneToMany(mappedBy = "versionApi", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnore
    private List<Documentacion> documentaciones;

    @OneToOne(mappedBy = "versionApi", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnore
    private ContratoApi contratoApi;
    
    // Constructores
    public VersionApi() {}
    
    public VersionApi(Integer idVersion, String version, EstadoVersion estadoVersion,
                      LocalDate fechaPublicacion, Api api) {
        this.idVersion = idVersion;
        this.version = version;
        this.estadoVersion = estadoVersion;
        this.fechaPublicacion = fechaPublicacion;
        this.api = api;
    }

    public VersionApi(String version, EstadoVersion estadoVersion, Api api) {
        this.version = version;
        this.estadoVersion = estadoVersion;
        this.api = api;
    }

    // Getters y Setters
    public Integer getIdVersion() {
        return idVersion;
    }
    
    public void setIdVersion(Integer idVersion) {
        this.idVersion = idVersion;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public EstadoVersion getEstadoVersion() {
        return estadoVersion;
    }
    
    public void setEstadoVersion(EstadoVersion estadoVersion) {
        this.estadoVersion = estadoVersion;
    }
    
    public LocalDate getFechaPublicacion() {
        return fechaPublicacion;
    }
    
    public void setFechaPublicacion(LocalDate fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }
    
    public Api getApi() {
        return api;
    }
    
    public void setApi(Api api) {
        this.api = api;
    }

    public List<Documentacion> getDocumentaciones() {
        return documentaciones;
    }

    public void setDocumentaciones(List<Documentacion> documentaciones) {
        this.documentaciones = documentaciones;
    }

    public ContratoApi getContratoApi() {
        return contratoApi;
    }

    public void setContratosApi(ContratoApi contratoApi) {
        this.contratoApi = contratoApi;
    }
}
