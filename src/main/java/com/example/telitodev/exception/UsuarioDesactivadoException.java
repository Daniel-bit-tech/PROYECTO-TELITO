package com.example.telitodev.exception;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Excepción personalizada para usuarios desactivados
 */
public class UsuarioDesactivadoException extends UsernameNotFoundException {
    
    public UsuarioDesactivadoException(String msg) {
        super(msg);
    }
    
    public UsuarioDesactivadoException(String msg, Throwable cause) {
        super(msg, cause);
    }
}