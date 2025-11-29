package com.example.telitodev.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "sol_acceso_equipo")
public class SolAccesoEquipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idSolicitudEquipo")
    private Integer idSolicitudEquipo;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 50)
    private String apellido;

    @Column(name = "correo", nullable = false, length = 150)
    private String correo;

    @Column(name = "dni", nullable = false, length = 8, columnDefinition = "CHAR(8)")
    private String dni;

    @Column(name = "detalles_solicitud", columnDefinition = "TEXT")
    private String detallesSolicitud;

    @Column(name = "motivo_integracion", nullable = false, columnDefinition = "TEXT")
    private String motivoIntegracion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoSolicitud estado;

    public enum Rol {INTERNO, EXTERNO}
    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 20)
    private Rol rol;

    @Column(name = "fecha_solicitud", nullable = false)
    private Timestamp fechaSolicitud;

    @Column(name = "fecha_revision")
    private Timestamp fechaRevision;

    @Column(name = "comentarios_revisor", columnDefinition = "TEXT")
    private String comentariosRevisor;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuario_solicitante", nullable = false, insertable = true, updatable = true)
    private Usuario usuarioSolicitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idEquipo_destino", nullable = false, insertable = true, updatable = true)
    private Equipo equipoDestino;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuario_revisor", insertable = false, updatable = true)
    private Usuario usuarioRevisor;

    // Constructores
    public SolAccesoEquipo() {
        this.estado = EstadoSolicitud.PENDIENTE;
        this.fechaSolicitud = new Timestamp(System.currentTimeMillis());
    }

    public SolAccesoEquipo(String nombre, String apellido, String correo, String dni,
                        String motivoIntegracion, Usuario usuarioSolicitante, Equipo equipoDestino) {
        this();
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.dni = dni;
        this.motivoIntegracion = motivoIntegracion;
        this.usuarioSolicitante = usuarioSolicitante;
        this.equipoDestino = equipoDestino;
    }

    // Getters y Setters
    public Integer getIdSolicitudEquipo() {
        return idSolicitudEquipo;
    }
    public void setIdSolicitudEquipo(Integer idSolicitudEquipo) {
        this.idSolicitudEquipo = idSolicitudEquipo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getDetallesSolicitud() {
        return detallesSolicitud;
    }

    public void setDetallesSolicitud(String detallesSolicitud) {
        this.detallesSolicitud = detallesSolicitud;
    }

    public String getMotivoIntegracion() {
        return motivoIntegracion;
    }

    public void setMotivoIntegracion(String motivoIntegracion) {
        this.motivoIntegracion = motivoIntegracion;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }

    public Rol getRol() {
        return rol;
    }
    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public Timestamp getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(Timestamp fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public Timestamp getFechaRevision() {
        return fechaRevision;
    }

    public void setFechaRevision(Timestamp fechaRevision) {
        this.fechaRevision = fechaRevision;
    }

    public String getComentariosRevisor() {
        return comentariosRevisor;
    }

    public void setComentariosRevisor(String comentariosRevisor) {
        this.comentariosRevisor = comentariosRevisor;
    }

    public Usuario getUsuarioSolicitante() {
        return usuarioSolicitante;
    }

    public void setUsuarioSolicitante(Usuario usuarioSolicitante) {
        this.usuarioSolicitante = usuarioSolicitante;
    }

    public Equipo getEquipoDestino() {
        return equipoDestino;
    }
    public void setEquipoDestino(Equipo equipoDestino) {
        this.equipoDestino = equipoDestino;
    }

    public Usuario getUsuarioRevisor() {
        return usuarioRevisor;
    }

    public void setUsuarioRevisor(Usuario usuarioRevisor) {
        this.usuarioRevisor = usuarioRevisor;
    }

    // Enum para el estado
    public enum EstadoSolicitud {
        PENDIENTE,
        APROBADA,
        RECHAZADA
    }

    // Métodos auxiliares
    public boolean isPendiente() {
        return EstadoSolicitud.PENDIENTE.equals(this.estado);
    }

    public boolean isAprobada() {
        return EstadoSolicitud.APROBADA.equals(this.estado);
    }

    public boolean isRechazada() {
        return EstadoSolicitud.RECHAZADA.equals(this.estado);
    }

    @Override
    public String toString() {
        return "SolAccesoOrg{" +
                "idSolicitudEquipo=" + idSolicitudEquipo +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", correo='" + correo + '\'' +
                ", dni='" + dni + '\'' +
                ", estado=" + estado +
                ", fechaSolicitud=" + fechaSolicitud +
                '}';
    }
}