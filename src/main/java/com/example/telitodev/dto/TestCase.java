package com.example.telitodev.dto;

import java.util.HashMap;
import java.util.Map;

public class TestCase {
    private Long id;
    private String nombre;
    private String descripcion;
    private String method;
    private String endpoint;
    private Map<String, Object> payload;
    private Map<String, String> expectedResponse;

    // Constructores
    public TestCase() {
    }

    public TestCase(Long id, String nombre, String descripcion, String method, String endpoint) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.method = method;
        this.endpoint = endpoint;
        this.payload = new HashMap<>();
        this.expectedResponse = new HashMap<>();
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

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public Map<String, String> getExpectedResponse() {
        return expectedResponse;
    }

    public void setExpectedResponse(Map<String, String> expectedResponse) {
        this.expectedResponse = expectedResponse;
    }
}
