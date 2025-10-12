
package com.example.telitodev.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "adjunto")
public class Adjunto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAdjunto")
    private Integer idAdjunto;

    @Column(name = "nombre", length = 200)
    private String nombre;

    @Lob
    @Column(name = "archivo", columnDefinition = "LONGBLOB")
    private byte[] archivo;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name="idComentario")
    private Comentario comentario;

    // Constructor vacío
    public Adjunto() {}

    // Constructor con parámetros
    public Adjunto(String nombre, byte[] archivo, Comentario comentario) {
        this.nombre = nombre;
        this.archivo = archivo;
        this.comentario = comentario;
    }


    public Integer getIdAdjunto() {
        return idAdjunto;
    }

    public void setIdAdjunto(Integer idAdjunto) {
        this.idAdjunto = idAdjunto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public byte[] getArchivo() {
        return archivo;
    }

    public void setArchivo(byte[] archivo) {
        this.archivo = archivo;
    }

    public Comentario getComentario() {
        return comentario;
    }

    public void setComentario(Comentario comentario) {
        this.comentario = comentario;
    }
}
