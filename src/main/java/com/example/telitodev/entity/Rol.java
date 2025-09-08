package com.example.telitodev.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "rol")
public class Rol {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idRol")
    private Integer idRol;
    
    @Column(name = "nombre_rol", nullable = false, length = 50)
    private String nombreRol;
    
    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;
    
    @OneToMany(mappedBy = "rol", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Usuario> usuarios;
    
    // Constructores
    public Rol() {}
    
    public Rol(String nombreRol, String descripcion) {
        this.nombreRol = nombreRol;
        this.descripcion = descripcion;
    }
    
    // Getters y Setters
    public Integer getIdRol() {
        return idRol;
    }
    
    public void setIdRol(Integer idRol) {
        this.idRol = idRol;
    }
    
    public String getNombreRol() {
        return nombreRol;
    }
    
    public void setNombreRol(String nombreRol) {
        this.nombreRol = nombreRol;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public List<Usuario> getUsuarios() {
        return usuarios;
    }
    
    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }
}
