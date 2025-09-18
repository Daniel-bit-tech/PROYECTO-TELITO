package com.example.telitodev.handler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.ui.Model;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpClientErrorException.BadRequest.class)
    public String handleBadRequestException(HttpClientErrorException.BadRequest ex, Model model) {
//        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        model.addAttribute("titulo", "Solicitud inválida");
        model.addAttribute("mensaje", ex.getMessage());
        model.addAttribute("status", ex.getStatusCode());
        return "error/error";
    }

    @ExceptionHandler(HttpClientErrorException.NotFound.class)
    public String handleNotFoundException(HttpClientErrorException.NotFound ex, Model model) {
//        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        model.addAttribute("titulo", "Recurso no encontrado");
        model.addAttribute("mensaje", ex.getMessage());
        model.addAttribute("status", HttpStatus.NOT_FOUND.value());
        return "error/error";
    }

    @ExceptionHandler(HttpClientErrorException.Forbidden.class)
    public String handleException(HttpClientErrorException.Forbidden ex, Model model) {
//        return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
        model.addAttribute("titulo", "Acceso denegado");
        model.addAttribute("mensaje", ex.getMessage());
        model.addAttribute("status", HttpStatus.FORBIDDEN.value());
        return "error/error";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, Model model) {
//        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        model.addAttribute("titulo", "Upps! Error interno");
        model.addAttribute("mensaje", "Ha ocurrido un error inesperado");
        model.addAttribute("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return "error/error";
    }
}
