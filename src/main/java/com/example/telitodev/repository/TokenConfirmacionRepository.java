package com.example.telitodev.repository;

import com.example.telitodev.entity.TokenConfirmacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenConfirmacionRepository extends JpaRepository<TokenConfirmacion, Long> {
    
    /**
     * Buscar token por token y email
     */
    Optional<TokenConfirmacion> findByTokenAndEmailIgnoreCase(String token, String email);
    
    /**
     * Buscar tokens por email (puede haber múltiples intentos)
     */
    List<TokenConfirmacion> findByEmailIgnoreCaseOrderByFechaCreacionDesc(String email);
    
    /**
     * Buscar tokens por DNI
     */
    List<TokenConfirmacion> findByDniUsuarioOrderByFechaCreacionDesc(String dniUsuario);
    
    /**
     * Buscar token válido más reciente por email
     */
    @Query("SELECT t FROM TokenConfirmacion t WHERE LOWER(t.email) = LOWER(:email) " +
           "AND t.usado = false AND t.fechaExpiracion > :fechaActual " +
           "ORDER BY t.fechaCreacion DESC")
    Optional<TokenConfirmacion> findTokenValidoMasReciente(@Param("email") String email, 
                                                          @Param("fechaActual") LocalDateTime fechaActual);
    
    /**
     * Buscar tokens expirados
     */
    @Query("SELECT t FROM TokenConfirmacion t WHERE t.fechaExpiracion < :fechaActual")
    List<TokenConfirmacion> findTokensExpirados(@Param("fechaActual") LocalDateTime fechaActual);
    
    /**
     * Verificar si existe un token válido para un email
     */
    @Query("SELECT COUNT(t) > 0 FROM TokenConfirmacion t WHERE LOWER(t.email) = LOWER(:email) " +
           "AND t.usado = false AND t.fechaExpiracion > :fechaActual")
    boolean existeTokenValidoPorEmail(@Param("email") String email, @Param("fechaActual") LocalDateTime fechaActual);
    
    /**
     * Verificar si existe un token válido para un DNI
     */
    @Query("SELECT COUNT(t) > 0 FROM TokenConfirmacion t WHERE t.dniUsuario = :dni " +
           "AND t.usado = false AND t.fechaExpiracion > :fechaActual")
    boolean existeTokenValidoPorDni(@Param("dni") String dni, @Param("fechaActual") LocalDateTime fechaActual);
    
    /**
     * Marcar token como usado
     */
    @Modifying
    @Transactional
    @Query("UPDATE TokenConfirmacion t SET t.usado = true WHERE t.id = :id")
    int marcarTokenComoUsado(@Param("id") Long id);
    
    /**
     * Invalidar todos los tokens de un email
     */
    @Modifying
    @Transactional
    @Query("UPDATE TokenConfirmacion t SET t.usado = true WHERE LOWER(t.email) = LOWER(:email) AND t.usado = false")
    int invalidarTokensPorEmail(@Param("email") String email);
    
    /**
     * Invalidar todos los tokens de un DNI
     */
    @Modifying
    @Transactional
    @Query("UPDATE TokenConfirmacion t SET t.usado = true WHERE t.dniUsuario = :dni AND t.usado = false")
    int invalidarTokensPorDni(@Param("dni") String dni);
    
    /**
     * Eliminar tokens expirados (limpieza de base de datos)
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM TokenConfirmacion t WHERE t.fechaExpiracion < :fechaLimite")
    int eliminarTokensExpirados(@Param("fechaLimite") LocalDateTime fechaLimite);
    
    /**
     * Contar tokens activos por email en las últimas horas (para prevenir spam)
     */
    @Query("SELECT COUNT(t) FROM TokenConfirmacion t WHERE LOWER(t.email) = LOWER(:email) " +
           "AND t.fechaCreacion > :fechaLimite")
    int contarTokensRecientesPorEmail(@Param("email") String email, @Param("fechaLimite") LocalDateTime fechaLimite);
    
    /**
     * Contar tokens activos por IP en las últimas horas (para prevenir spam)
     */
    @Query("SELECT COUNT(t) FROM TokenConfirmacion t WHERE t.ipCreacion = :ip " +
           "AND t.fechaCreacion > :fechaLimite")
    int contarTokensRecientesPorIp(@Param("ip") String ip, @Param("fechaLimite") LocalDateTime fechaLimite);
    
    /**
     * Obtener todos los tokens válidos (no expirados y no usados) con información completa
     */
    @Query("SELECT t FROM TokenConfirmacion t WHERE t.usado = false AND t.fechaExpiracion > :fechaActual " +
           "ORDER BY t.fechaCreacion DESC")
    List<TokenConfirmacion> findTokensValidosConInfo(@Param("fechaActual") LocalDateTime fechaActual);
}