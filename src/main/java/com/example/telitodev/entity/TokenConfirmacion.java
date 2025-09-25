package com.example.telitodev.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tokens_confirmacion")
public class TokenConfirmacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "token", nullable = false, length = 6)
    private String token;
    
    @Column(name = "email", nullable = false)
    private String email;
    
    @Column(name = "dni_usuario", nullable = false)
    private String dniUsuario;
    
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;
    
    @Column(name = "usado", nullable = false)
    private Boolean usado = false;
    
    @Column(name = "fecha_uso")
    private LocalDateTime fechaUso;
    
    @Column(name = "ip_creacion")
    private String ipCreacion;
    
    // Datos temporales del usuario hasta confirmar
    @Column(name = "nombre_temporal", nullable = false)
    private String nombreTemporal;
    
    @Column(name = "apellido_paterno_temporal", nullable = false)
    private String apellidoPaternoTemporal;
    
    @Column(name = "apellido_materno_temporal")
    private String apellidoMaternoTemporal;
    
    @Column(name = "contrasena_temporal", nullable = false)
    private String contrasenaTemporal;
    
    @Column(name = "id_rol_temporal", nullable = false)
    private Integer idRolTemporal;

    // Constructores
    public TokenConfirmacion() {}
    
    public TokenConfirmacion(String token, String email, String dniUsuario, 
                           String nombreTemporal, String apellidoPaternoTemporal, 
                           String apellidoMaternoTemporal, String contrasenaTemporal, 
                           Integer idRolTemporal, String ipCreacion) {
        this.token = token;
        this.email = email;
        this.dniUsuario = dniUsuario;
        this.nombreTemporal = nombreTemporal;
        this.apellidoPaternoTemporal = apellidoPaternoTemporal;
        this.apellidoMaternoTemporal = apellidoMaternoTemporal;
        this.contrasenaTemporal = contrasenaTemporal;
        this.idRolTemporal = idRolTemporal;
        this.ipCreacion = ipCreacion;
        this.fechaCreacion = LocalDateTime.now();
        this.fechaExpiracion = LocalDateTime.now().plusMinutes(3); // Token válido por 3 minutos (para pruebas)
        this.usado = false;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDniUsuario() {
        return dniUsuario;
    }

    public void setDniUsuario(String dniUsuario) {
        this.dniUsuario = dniUsuario;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(LocalDateTime fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    public Boolean getUsado() {
        return usado;
    }

    public void setUsado(Boolean usado) {
        this.usado = usado;
    }

    public LocalDateTime getFechaUso() {
        return fechaUso;
    }

    public void setFechaUso(LocalDateTime fechaUso) {
        this.fechaUso = fechaUso;
    }

    public String getIpCreacion() {
        return ipCreacion;
    }

    public void setIpCreacion(String ipCreacion) {
        this.ipCreacion = ipCreacion;
    }

    public String getNombreTemporal() {
        return nombreTemporal;
    }

    public void setNombreTemporal(String nombreTemporal) {
        this.nombreTemporal = nombreTemporal;
    }

    public String getApellidoPaternoTemporal() {
        return apellidoPaternoTemporal;
    }

    public void setApellidoPaternoTemporal(String apellidoPaternoTemporal) {
        this.apellidoPaternoTemporal = apellidoPaternoTemporal;
    }

    public String getApellidoMaternoTemporal() {
        return apellidoMaternoTemporal;
    }

    public void setApellidoMaternoTemporal(String apellidoMaternoTemporal) {
        this.apellidoMaternoTemporal = apellidoMaternoTemporal;
    }

    public String getContrasenaTemporal() {
        return contrasenaTemporal;
    }

    public void setContrasenaTemporal(String contrasenaTemporal) {
        this.contrasenaTemporal = contrasenaTemporal;
    }

    public Integer getIdRolTemporal() {
        return idRolTemporal;
    }

    public void setIdRolTemporal(Integer idRolTemporal) {
        this.idRolTemporal = idRolTemporal;
    }

    // Métodos de utilidad
    public boolean estaExpirado() {
        return LocalDateTime.now().isAfter(this.fechaExpiracion);
    }

    public boolean estaUsado() {
        return this.usado != null && this.usado;
    }

    public boolean esValido() {
        return !estaExpirado() && !estaUsado();
    }

    @Override
    public String toString() {
        return "TokenConfirmacion{" +
                "id=" + id +
                ", token='" + token + '\'' +
                ", email='" + email + '\'' +
                ", dniUsuario='" + dniUsuario + '\'' +
                ", fechaCreacion=" + fechaCreacion +
                ", fechaExpiracion=" + fechaExpiracion +
                ", usado=" + usado +
                ", nombreTemporal='" + nombreTemporal + '\'' +
                '}';
    }
}