package com.example.telitodev.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "api")
public class Api {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAPI")
    private Integer idApi;
    
    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;
    
    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;
    
    @Column(name = "fecha_creacion")
    private Timestamp fechaCreacion;
    
    @Column(name = "dominio", length = 50)
    private String dominio;
    
    @Column(name = "tag", length = 45)
    private String tag;
    
    @Column(name = "endpointURL", nullable = false, length = 45)
    private String endpointUrl;
    
    // Relaciones
    @OneToMany(mappedBy = "api", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ApiHasEntorno> apiHasEntornos;
    
    @OneToMany(mappedBy = "api", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ContratoApi> contratosApi;
    
    @OneToMany(mappedBy = "api", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CredencialApi> credencialesApi;
    
    @OneToMany(mappedBy = "api", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Documentacion> documentaciones;
    
    @OneToMany(mappedBy = "api", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EstadoApi> estadosApi;
    
    @OneToMany(mappedBy = "api", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Feedback> feedbacks;
    
    @OneToMany(mappedBy = "api", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LogApi> logsApi;
    
    @OneToMany(mappedBy = "api", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MetricaApi> metricasApi;
    
    @OneToMany(mappedBy = "api", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reporte> reportes;
    
    @OneToMany(mappedBy = "api", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Sdk> sdks;
    
    @OneToMany(mappedBy = "api", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SolicitudAcceso> solicitudesAcceso;
    
    @OneToMany(mappedBy = "api", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ticket> tickets;
    
    @OneToMany(mappedBy = "api", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VersionApi> versionesApi;
    
    // Constructores
    public Api() {}
    
    public Api(String nombre, String descripcion, Timestamp fechaCreacion, 
               String dominio, String tag, String endpointUrl) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaCreacion = fechaCreacion;
        this.dominio = dominio;
        this.tag = tag;
        this.endpointUrl = endpointUrl;
    }
    
    // Getters y Setters
    public Integer getIdApi() {
        return idApi;
    }
    
    public void setIdApi(Integer idApi) {
        this.idApi = idApi;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public Timestamp getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(Timestamp fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public String getDominio() {
        return dominio;
    }
    
    public void setDominio(String dominio) {
        this.dominio = dominio;
    }
    
    public String getTag() {
        return tag;
    }
    
    public void setTag(String tag) {
        this.tag = tag;
    }
    
    public String getEndpointUrl() {
        return endpointUrl;
    }
    
    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }
    
    public List<ApiHasEntorno> getApiHasEntornos() {
        return apiHasEntornos;
    }
    
    public void setApiHasEntornos(List<ApiHasEntorno> apiHasEntornos) {
        this.apiHasEntornos = apiHasEntornos;
    }
    
    public List<ContratoApi> getContratosApi() {
        return contratosApi;
    }
    
    public void setContratosApi(List<ContratoApi> contratosApi) {
        this.contratosApi = contratosApi;
    }
    
    public List<CredencialApi> getCredencialesApi() {
        return credencialesApi;
    }
    
    public void setCredencialesApi(List<CredencialApi> credencialesApi) {
        this.credencialesApi = credencialesApi;
    }
    
    public List<Documentacion> getDocumentaciones() {
        return documentaciones;
    }
    
    public void setDocumentaciones(List<Documentacion> documentaciones) {
        this.documentaciones = documentaciones;
    }
    
    public List<EstadoApi> getEstadosApi() {
        return estadosApi;
    }
    
    public void setEstadosApi(List<EstadoApi> estadosApi) {
        this.estadosApi = estadosApi;
    }
    
    public List<Feedback> getFeedbacks() {
        return feedbacks;
    }
    
    public void setFeedbacks(List<Feedback> feedbacks) {
        this.feedbacks = feedbacks;
    }
    
    public List<LogApi> getLogsApi() {
        return logsApi;
    }
    
    public void setLogsApi(List<LogApi> logsApi) {
        this.logsApi = logsApi;
    }
    
    public List<MetricaApi> getMetricasApi() {
        return metricasApi;
    }
    
    public void setMetricasApi(List<MetricaApi> metricasApi) {
        this.metricasApi = metricasApi;
    }
    
    public List<Reporte> getReportes() {
        return reportes;
    }
    
    public void setReportes(List<Reporte> reportes) {
        this.reportes = reportes;
    }
    
    public List<Sdk> getSdks() {
        return sdks;
    }
    
    public void setSdks(List<Sdk> sdks) {
        this.sdks = sdks;
    }
    
    public List<SolicitudAcceso> getSolicitudesAcceso() {
        return solicitudesAcceso;
    }
    
    public void setSolicitudesAcceso(List<SolicitudAcceso> solicitudesAcceso) {
        this.solicitudesAcceso = solicitudesAcceso;
    }
    
    public List<Ticket> getTickets() {
        return tickets;
    }
    
    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }
    
    public List<VersionApi> getVersionesApi() {
        return versionesApi;
    }
    
    public void setVersionesApi(List<VersionApi> versionesApi) {
        this.versionesApi = versionesApi;
    }
}
