package com.example.telitodev.controller.api;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/session-debug")
public class SessionDebugController {

    @Autowired
    private SessionRegistry sessionRegistry;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getSessionInfo(
            HttpServletRequest request,
            HttpSession session,
            Authentication authentication) {

        Map<String, Object> info = new HashMap<>();

        // Información básica de sesión
        info.put("sessionId", session.getId());
        info.put("sessionNew", session.isNew());
        info.put("sessionCreationTime", new Date(session.getCreationTime()));
        info.put("sessionLastAccessedTime", new Date(session.getLastAccessedTime()));
        info.put("sessionMaxInactiveInterval", session.getMaxInactiveInterval());

        // Información de autenticación
        if (authentication != null) {
            info.put("authenticated", true);
            info.put("username", authentication.getName());
            info.put("authorities", authentication.getAuthorities().toString());
        } else {
            info.put("authenticated", false);
        }

        // Cookies
        Cookie[] cookies = request.getCookies();
        Map<String, String> cookiesMap = new HashMap<>();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookiesMap.put(cookie.getName(), 
                    String.format("Value: %s, MaxAge: %d, Path: %s, Secure: %s, HttpOnly: %s",
                        cookie.getValue().substring(0, Math.min(10, cookie.getValue().length())) + "...",
                        cookie.getMaxAge(),
                        cookie.getPath(),
                        cookie.getSecure(),
                        cookie.isHttpOnly()));
            }
        }
        info.put("cookies", cookiesMap);

        // Atributos de sesión
        Map<String, String> attributes = new HashMap<>();
        Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();
            Object value = session.getAttribute(name);
            attributes.put(name, value != null ? value.toString() : "null");
        }
        info.put("sessionAttributes", attributes);

        // SessionRegistry info
        try {
            info.put("sessionRegistryUsers", sessionRegistry.getAllPrincipals().size());
            info.put("sessionRegistrySessions", 
                sessionRegistry.getAllPrincipals().stream()
                    .mapToInt(p -> sessionRegistry.getAllSessions(p, false).size())
                    .sum());
        } catch (Exception e) {
            info.put("sessionRegistryError", e.getMessage());
        }

        // Verificar Spring Session en base de datos
        try {
            Long sessionCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM SPRING_SESSION WHERE PRINCIPAL_NAME = ?",
                Long.class,
                authentication != null ? authentication.getName() : "anonymous");
            info.put("dbSessionCount", sessionCount);

            // Obtener detalles de sesiones en DB
            String sql = "SELECT SESSION_ID, CREATION_TIME, LAST_ACCESS_TIME, " +
                        "EXPIRY_TIME, MAX_INACTIVE_INTERVAL " +
                        "FROM SPRING_SESSION WHERE PRINCIPAL_NAME = ?";
            List<Map<String, Object>> dbSessions = jdbcTemplate.queryForList(
                sql, 
                authentication != null ? authentication.getName() : "anonymous");
            info.put("dbSessions", dbSessions);

        } catch (Exception e) {
            info.put("dbError", e.getMessage());
        }

        return ResponseEntity.ok(info);
    }

    @GetMapping("/test-persistence")
    public ResponseEntity<Map<String, Object>> testPersistence(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        // Crear o actualizar un contador de prueba
        Integer testCounter = (Integer) session.getAttribute("TEST_COUNTER");
        if (testCounter == null) {
            testCounter = 0;
        }
        testCounter++;
        session.setAttribute("TEST_COUNTER", testCounter);
        
        response.put("sessionId", session.getId());
        response.put("testCounter", testCounter);
        response.put("message", "Si este contador aumenta cada vez que llamas este endpoint, " +
                               "la sesión está funcionando correctamente.");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify-tables")
    public ResponseEntity<Map<String, Object>> verifyTables() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verificar tabla SPRING_SESSION
            Long sessionCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM SPRING_SESSION", Long.class);
            response.put("springSessionCount", sessionCount);
            response.put("springSessionExists", true);

            // Verificar tabla SPRING_SESSION_ATTRIBUTES
            Long attrCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM SPRING_SESSION_ATTRIBUTES", Long.class);
            response.put("springSessionAttributesCount", attrCount);
            response.put("springSessionAttributesExists", true);

            // Obtener sesiones activas (no expiradas)
            String sql = "SELECT COUNT(*) FROM SPRING_SESSION " +
                        "WHERE EXPIRY_TIME > UNIX_TIMESTAMP(NOW()) * 1000";
            Long activeSessions = jdbcTemplate.queryForObject(sql, Long.class);
            response.put("activeSessionsCount", activeSessions);

            response.put("status", "OK");
            response.put("message", "Tablas de Spring Session están correctamente configuradas");

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
            response.put("message", "Error verificando tablas. Posiblemente no existen. " +
                                  "Ejecuta el script verificar_spring_session.sql");
        }
        
        return ResponseEntity.ok(response);
    }
}
