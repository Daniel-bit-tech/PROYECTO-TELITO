package com.example.telitodev.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.List;


@Entity
@Table(name = "tag")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTag")
    private Integer idTag;

    @Column(name = "nombre", length = 20)
    private String nombre;

    //Relaciones

    @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Api> apisTag;


    //Get y Set
    public Integer getIdTag() {
        return idTag;
    }
    public void setIdTag(Integer idTag) {
        this.idTag = idTag;
    }

    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Api> getApisTag() {
        return apisTag;
    }
    public void setApisTag(List<Api> apisTag) {
        this.apisTag = apisTag;
    }
}
