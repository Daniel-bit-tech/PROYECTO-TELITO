package com.example.telitodev.handler;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.client.HttpClientErrorException;

@ControllerAdvice
public class GlobalExceptionHandler {

    /// ERRORES INTERNOS ///



    @ExceptionHandler(ResponseStatusException.class)
    public String handleResponseStatus(ResponseStatusException ex, Model model) {
        model.addAttribute("titulo", "Error " + ex.getStatusCode().value());
        model.addAttribute("mensaje", ex.getReason() != null ? ex.getReason() : "Error en la solicitud");
        model.addAttribute("status", ex.getStatusCode().value());
        return "error/error"; // vista Thymeleaf
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException ex, Model model) {
        model.addAttribute("titulo", "Solicitud inválida");
        model.addAttribute("mensaje", ex.getMessage());
        model.addAttribute("status", HttpStatus.BAD_REQUEST.value());
        return "error/error";
    }

    // Otros errores no capturados (en un futuro)
//    @ExceptionHandler(Exception.class)
//    public String handleGeneric(Exception ex, Model model) {
//        model.addAttribute("titulo", "Upps! Error interno");
//        model.addAttribute("mensaje", "Ha ocurrido un error inesperado");
//        model.addAttribute("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
//        return "error/error";
//    }

    /// ERRORES EXTERNOS para APIs ///

    @ExceptionHandler(HttpClientErrorException.class)
    public String handleExternalApiError(HttpClientErrorException ex, Model model) {
        model.addAttribute("titulo", "Error al consumir API externa");
        model.addAttribute("mensaje", ex.getMessage());
        model.addAttribute("status", ex.getStatusCode().value());
        return "error/error";
    }
}
