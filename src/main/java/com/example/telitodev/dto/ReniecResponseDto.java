package com.example.telitodev.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para la respuesta de la API de RENIEC (apiperu.dev)
 * Contiene los datos de una persona consultada por DNI
 */
public class ReniecResponseDto {

    private boolean success;
    private ReniecData data;
    private String message;

    public ReniecResponseDto() {
    }

    public ReniecResponseDto(boolean success, ReniecData data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    // Getters y Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public ReniecData getData() {
        return data;
    }

    public void setData(ReniecData data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Clase interna que representa los datos de la persona
     */
    public static class ReniecData {
        @JsonProperty("numero")
        private String dni;
        
        @JsonProperty("nombre_completo")
        private String nombreCompleto;
        
        private String nombres;
        
        @JsonProperty("apellido_paterno")
        private String apellidoPaterno;
        
        @JsonProperty("apellido_materno")
        private String apellidoMaterno;
        
        @JsonProperty("codigo_verificacion")
        private String codigoVerificacion;

        public ReniecData() {
        }

        // Getters y Setters
        public String getDni() {
            return dni;
        }

        public void setDni(String dni) {
            this.dni = dni;
        }

        public String getNombreCompleto() {
            return nombreCompleto;
        }

        public void setNombreCompleto(String nombreCompleto) {
            this.nombreCompleto = nombreCompleto;
        }

        public String getNombres() {
            return nombres;
        }

        public void setNombres(String nombres) {
            this.nombres = nombres;
        }

        public String getApellidoPaterno() {
            return apellidoPaterno;
        }

        public void setApellidoPaterno(String apellidoPaterno) {
            this.apellidoPaterno = apellidoPaterno;
        }

        public String getApellidoMaterno() {
            return apellidoMaterno;
        }

        public void setApellidoMaterno(String apellidoMaterno) {
            this.apellidoMaterno = apellidoMaterno;
        }

        public String getCodigoVerificacion() {
            return codigoVerificacion;
        }

        public void setCodigoVerificacion(String codigoVerificacion) {
            this.codigoVerificacion = codigoVerificacion;
        }
    }
}
