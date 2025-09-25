package com.example.telitodev.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa las actividades realizadas por administradores
 * para auditoría y seguimiento del sistema
 */
@Entity
@Table(name = "actividad_admin")
public class ActividadAdmin {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "accion", nullable = false, length = 50)
    private String accion;
    
    @Column(name = "descripcion", nullable = false, length = 500)
    private String descripcion;
    
    @Column(name = "usuario_admin_dni", nullable = false, length = 20)
    private String usuarioAdminDni;
    
    @Column(name = "usuario_afectado_dni", length = 20)
    private String usuarioAfectadoDni;
    
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;
    
    @Column(name = "detalles", columnDefinition = "JSON")
    private String detalles;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    // Foreign Key relationships (opcional para consultas)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_admin_dni", referencedColumnName = "dni", insertable = false, updatable = false)
    private Usuario usuarioAdmin;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_afectado_dni", referencedColumnName = "dni", insertable = false, updatable = false)
    private Usuario usuarioAfectado;
    
    // Constructores
    public ActividadAdmin() {
        this.fechaHora = LocalDateTime.now();
    }
    
    public ActividadAdmin(String accion, String descripcion, String usuarioAdminDni) {
        this();
        this.accion = accion;
        this.descripcion = descripcion;
        this.usuarioAdminDni = usuarioAdminDni;
    }
    
    public ActividadAdmin(String accion, String descripcion, String usuarioAdminDni, String usuarioAfectadoDni) {
        this(accion, descripcion, usuarioAdminDni);
        this.usuarioAfectadoDni = usuarioAfectadoDni;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getAccion() {
        return accion;
    }
    
    public void setAccion(String accion) {
        this.accion = accion;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getUsuarioAdminDni() {
        return usuarioAdminDni;
    }
    
    public void setUsuarioAdminDni(String usuarioAdminDni) {
        this.usuarioAdminDni = usuarioAdminDni;
    }
    
    public String getUsuarioAfectadoDni() {
        return usuarioAfectadoDni;
    }
    
    public void setUsuarioAfectadoDni(String usuarioAfectadoDni) {
        this.usuarioAfectadoDni = usuarioAfectadoDni;
    }
    
    public LocalDateTime getFechaHora() {
        return fechaHora;
    }
    
    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }
    
    public String getDetalles() {
        return detalles;
    }
    
    public void setDetalles(String detalles) {
        this.detalles = detalles;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public Usuario getUsuarioAdmin() {
        return usuarioAdmin;
    }
    
    public void setUsuarioAdmin(Usuario usuarioAdmin) {
        this.usuarioAdmin = usuarioAdmin;
    }
    
    public Usuario getUsuarioAfectado() {
        return usuarioAfectado;
    }
    
    public void setUsuarioAfectado(Usuario usuarioAfectado) {
        this.usuarioAfectado = usuarioAfectado;
    }
    
    // Métodos de utilidad
    public String getTiempoTranscurrido() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime fecha = this.fechaHora;
        
        long minutos = java.time.Duration.between(fecha, ahora).toMinutes();
        
        if (minutos < 60) {
            return "Hace " + minutos + " minutos";
        } else if (minutos < 1440) { // menos de 24 horas
            long horas = minutos / 60;
            return "Hace " + horas + " hora" + (horas > 1 ? "s" : "");
        } else {
            long dias = minutos / 1440;
            return "Hace " + dias + " día" + (dias > 1 ? "s" : "");
        }
    }
    
    @Override
    public String toString() {
        return "ActividadAdmin{" +
                "id=" + id +
                ", accion='" + accion + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", usuarioAdminDni='" + usuarioAdminDni + '\'' +
                ", fechaHora=" + fechaHora +
                '}';
    }
}