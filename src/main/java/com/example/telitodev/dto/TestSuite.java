package com.example.telitodev.dto;

import com.example.telitodev.dto.TestCase;


import java.util.List;

public class TestSuite {
    private Long id;
    private String nombre;
    private String descripcion;
    private List<TestCase> tests;

    // Constructores
    public TestSuite() {
    }

    public TestSuite(Long id, String nombre, String descripcion, List<TestCase> tests) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tests = tests;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public List<TestCase> getTests() {
        return tests;
    }

    public void setTests(List<TestCase> tests) {
        this.tests = tests;
    }
}
