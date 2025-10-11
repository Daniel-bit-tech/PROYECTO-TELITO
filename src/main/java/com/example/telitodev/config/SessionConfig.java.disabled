package com.example.telitodev.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * Configuración de Spring Session para manejo avanzado de sesiones de administradores
 * 
 * Características implementadas:
 * - Almacenamiento de sesiones en base de datos
 * - Control de sesiones concurrentes
 * - Timeout configurable de sesiones
 * - Cookies seguras con HTTPOnly
 * - Monitoreo de sesiones activas
 */
@Configuration
@EnableSpringHttpSession
@EnableJdbcHttpSession(
    maxInactiveIntervalInSeconds = 3600, // 1 hora de inactividad
    tableName = "SPRING_SESSION",
    cleanupCron = "0 */5 * * * *" // Limpieza cada 5 minutos
)
public class SessionConfig {

    /**
     * Configuración de cookies de sesión seguras
     */
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        
        // Configuraciones de seguridad
        serializer.setCookieName("TELITODEV-SESSION");
        serializer.setSameSite("Strict"); // Protección CSRF  
        serializer.setCookiePath("/");
        serializer.setCookieMaxAge(3600); // 1 hora
        
        return serializer;
    }

    /**
     * Registry para control de sesiones concurrentes
     * Permite monitorear y controlar las sesiones activas por usuario
     */
    @Bean
    public SpringSessionBackedSessionRegistry sessionRegistry(FindByIndexNameSessionRepository sessionRepository) {
        return new SpringSessionBackedSessionRegistry<>(sessionRepository);
    }
}