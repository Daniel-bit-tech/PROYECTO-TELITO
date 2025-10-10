package com.example.telitodev.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "roadmap")
public class Roadmap {

    // ===== Enum anidado (solo estados que generan tramo) =====
    public enum EstadoEvolucion {
        PROXIMA,
        EN_DESARROLLO,
        NUEVA,
        OLD;
    }
    // ==========================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")               // BIGINT AUTO_INCREMENT
    private Long id;

    // FK -> api(idAPI)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "api_id", referencedColumnName = "idAPI", nullable = false)
    private Api api;

    // Estado del tramo (persistido como texto)
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoEvolucion estado;

    // Rango del tramo (línea de tiempo)
    @Column(name = "inicio", nullable = false)
    private LocalDate inicio;

    @Column(name = "fin")
    private LocalDate fin;              // NULL = tramo abierto (vigente)

    // Quién hizo el cambio (FK -> usuario.dni CHAR(8))
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "usuario_dni",
            referencedColumnName = "dni",
            columnDefinition = "char(8)"
    )
    private Usuario usuario;

    // Auditoría mínima (DEFAULT CURRENT_TIMESTAMP en BD)
    @Column(name = "changed_at", insertable = false, updatable = false)
    private LocalDateTime changedAt;

    // Columna generada en BD para garantizar 1 tramo abierto por API (READ-ONLY)
    @Column(name = "abierto_flag", insertable = false, updatable = false)
    private Integer abiertoFlag;        // 1 si fin IS NULL; NULL si cerrado

    // ===== Constructores =====
    public Roadmap() {}

    public Roadmap(Api api, EstadoEvolucion estado, LocalDate inicio, LocalDate fin, Usuario usuario) {
        this.api = api;
        this.estado = estado;
        this.inicio = inicio;
        this.fin = fin;
        this.usuario = usuario;
    }

    // ===== Getters & Setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Api getApi() { return api; }
    public void setApi(Api api) { this.api = api; }

    public EstadoEvolucion getEstado() { return estado; }
    public void setEstado(EstadoEvolucion estado) { this.estado = estado; }

    public LocalDate getInicio() { return inicio; }
    public void setInicio(LocalDate inicio) { this.inicio = inicio; }

    public LocalDate getFin() { return fin; }
    public void setFin(LocalDate fin) { this.fin = fin; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }

    public Integer getAbiertoFlag() { return abiertoFlag; }
    public void setAbiertoFlag(Integer abiertoFlag) { this.abiertoFlag = abiertoFlag; }

    // ===== toString() breve =====
    @Override
    public String toString() {
        return "Roadmap{" +
                "id=" + id +
                ", api=" + (api != null ? api.getIdApi() : null) +
                ", estado=" + estado +
                ", inicio=" + inicio +
                ", fin=" + fin +
                ", usuario=" + (usuario != null ? usuario.getDni() : null) +
                '}';
    }
}
