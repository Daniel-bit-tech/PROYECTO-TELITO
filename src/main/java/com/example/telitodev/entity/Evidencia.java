
package com.example.telitodev.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "evidencia")
public class Evidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Se auto-generará el idEvidencia
    @Column(name = "idEvidencia")
    private int idEvidencia;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Lob
    @Column(name = "evidencia", nullable = false)
    private byte[] evidencia;

    @Lob
    @Column(name = "descripcion", length = 200)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idReporte", referencedColumnName = "idReporte", nullable = false)
    private Reporte reporte;


    // Constructores
    public Evidencia() {}

    public Evidencia(String nombre, byte[] evidencia, String descripcion, Reporte reporte) {
        this.nombre = nombre;
        this.evidencia = evidencia;
        this.descripcion = descripcion;
        this.reporte = reporte;
    }

    public int getIdEvidencia() {
        return idEvidencia;
    }

    public void setIdEvidencia(int idEvidencia) {
        this.idEvidencia = idEvidencia;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public byte[] getEvidencia() {
        return evidencia;
    }

    public void setEvidencia(byte[] evidencia) {
        this.evidencia = evidencia;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Reporte getReporte() {
        return reporte;
    }

    public void setReporte(Reporte reporte) {
        this.reporte = reporte;
    }
}
