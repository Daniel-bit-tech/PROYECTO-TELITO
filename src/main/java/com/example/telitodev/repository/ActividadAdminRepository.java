package com.example.telitodev.repository;

import com.example.telitodev.entity.ActividadAdmin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para operaciones CRUD de ActividadAdmin
 * Incluye consultas personalizadas para auditoría y reportes
 */
@Repository
public interface ActividadAdminRepository extends JpaRepository<ActividadAdmin, Long> {
    
    /**
     * Obtiene las últimas actividades ordenadas por fecha (más recientes primero)
     * @param pageable configuración de paginación
     * @return página de actividades
     */
    @Query("SELECT a FROM ActividadAdmin a ORDER BY a.fechaHora DESC")
    Page<ActividadAdmin> findLatestActivities(Pageable pageable);
    
    /**
     * Encuentra todas las actividades de un administrador específico
     * @param usuarioAdminDni DNI del administrador
     * @param pageable configuración de paginación
     * @return página de actividades del admin
     */
    Page<ActividadAdmin> findByUsuarioAdminDniOrderByFechaHoraDesc(String usuarioAdminDni, Pageable pageable);
    
    /**
     * Encuentra actividades por tipo de acción
     * @param accion tipo de acción (CREAR_USUARIO, BANEAR_USUARIO, etc.)
     * @param pageable configuración de paginación
     * @return página de actividades del tipo especificado
     */
    Page<ActividadAdmin> findByAccionOrderByFechaHoraDesc(String accion, Pageable pageable);
    
    /**
     * Encuentra actividades que afectan a un usuario específico
     * @param usuarioAfectadoDni DNI del usuario afectado
     * @param pageable configuración de paginación
     * @return página de actividades relacionadas al usuario
     */
    Page<ActividadAdmin> findByUsuarioAfectadoDniOrderByFechaHoraDesc(String usuarioAfectadoDni, Pageable pageable);
    
    /**
     * Obtiene las últimas N actividades sin paginación
     * @param limit número máximo de actividades a retornar
     * @return lista de actividades recientes
     */
    @Query("SELECT a FROM ActividadAdmin a ORDER BY a.fechaHora DESC LIMIT :limit")
    List<ActividadAdmin> findTopActivities(@Param("limit") int limit);
    
    /**
     * Encuentra actividades en un rango de fechas
     * @param fechaInicio fecha de inicio del rango
     * @param fechaFin fecha de fin del rango
     * @param pageable configuración de paginación
     * @return página de actividades en el rango de fechas
     */
    @Query("SELECT a FROM ActividadAdmin a WHERE a.fechaHora BETWEEN :fechaInicio AND :fechaFin ORDER BY a.fechaHora DESC")
    Page<ActividadAdmin> findByFechaHoraBetween(
        @Param("fechaInicio") LocalDateTime fechaInicio, 
        @Param("fechaFin") LocalDateTime fechaFin, 
        Pageable pageable
    );
    
    /**
     * Cuenta actividades por tipo de acción
     * @param accion tipo de acción
     * @return número de actividades del tipo especificado
     */
    long countByAccion(String accion);
    
    /**
     * Cuenta actividades de un administrador
     * @param usuarioAdminDni DNI del administrador
     * @return número de actividades realizadas por el admin
     */
    long countByUsuarioAdminDni(String usuarioAdminDni);
    
    /**
     * Obtiene las últimas actividades de hoy para el dashboard
     * @return lista de actividades de hoy
     */
    @Query("SELECT a FROM ActividadAdmin a WHERE DATE(a.fechaHora) = CURRENT_DATE ORDER BY a.fechaHora DESC LIMIT 5")
    List<ActividadAdmin> findTodayActivitiesForDashboard();
    
    /**
     * Busca actividades por descripción (para filtros de búsqueda)
     * @param descripcion texto a buscar en la descripción
     * @param pageable configuración de paginación
     * @return página de actividades que contienen el texto
     */
    @Query("SELECT a FROM ActividadAdmin a WHERE LOWER(a.descripcion) LIKE LOWER(CONCAT('%', :descripcion, '%')) ORDER BY a.fechaHora DESC")
    Page<ActividadAdmin> findByDescripcionContainingIgnoreCase(@Param("descripcion") String descripcion, Pageable pageable);
    
    /**
     * Estadísticas de actividades por día (para gráficos)
     * @param dias número de días atrás a considerar
     * @return lista de conteos por fecha
     */
    @Query("SELECT DATE(a.fechaHora) as fecha, COUNT(a) as total FROM ActividadAdmin a WHERE a.fechaHora >= :fechaInicio GROUP BY DATE(a.fechaHora) ORDER BY fecha DESC")
    List<Object[]> getActividadesPorDia(@Param("fechaInicio") LocalDateTime fechaInicio);
    
    /**
     * Elimina todas las actividades relacionadas con un usuario específico
     * @param usuarioAfectadoDni DNI del usuario afectado
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ActividadAdmin a WHERE a.usuarioAfectadoDni = :usuarioAfectadoDni")
    void deleteByUsuarioAfectadoDni(@Param("usuarioAfectadoDni") String usuarioAfectadoDni);
    
    /**
     * Elimina todas las actividades relacionadas con un usuario específico usando SQL nativo
     * @param usuarioAfectadoDni DNI del usuario afectado
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM actividad_admin WHERE usuario_afectado_dni = :usuarioAfectadoDni", nativeQuery = true)
    void deleteByUsuarioAfectadoDniNative(@Param("usuarioAfectadoDni") String usuarioAfectadoDni);
    
    /**
     * Cuenta las actividades relacionadas con un usuario específico
     * @param usuarioAfectadoDni DNI del usuario afectado
     * @return número de actividades relacionadas
     */
    long countByUsuarioAfectadoDni(String usuarioAfectadoDni);
}