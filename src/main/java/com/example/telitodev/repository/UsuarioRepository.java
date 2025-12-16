package com.example.telitodev.repository;

import com.example.telitodev.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    // Métodos existentes
    Optional<Usuario> findByCorreoAndEstado(String correo, Boolean estado);
    Usuario findByDni(String dni);
    Usuario findByCorreo(String correo);
    Usuario findByCorreoCorporativo(String correoCorporativo);

    // Métodos existentes


    // Métodos para paginación y filtros
    Page<Usuario> findByNombreContainingIgnoreCaseOrCorreoContainingIgnoreCase(
            String nombre, String correo, Pageable pageable);

    Page<Usuario> findByRol_NombreRol(String nombreRol, Pageable pageable);

    Page<Usuario> findByEstado(Boolean estado, Pageable pageable);

    // Métodos para búsqueda rápida
    List<Usuario> findTop10ByNombreContainingIgnoreCaseOrCorreoContainingIgnoreCase(
            String nombre, String correo);

    // Métodos para estadísticas
    long countByEstado(Boolean estado);

    // Método para verificar existencia por DNI
    boolean existsByDni(String dni);

    // Método para verificar existencia por correo
    boolean existsByCorreo(String correo);
    
    // Métodos para OAuth2
    Optional<Usuario> findByOauthProviderIdAndOauthProvider(String oauthProviderId, String oauthProvider);
    
    List<Usuario> findByTipoAcceso(Usuario.TipoAcceso tipoAcceso);
    
    // Buscar usuarios externos por proveedor
    List<Usuario> findByOauthProvider(String oauthProvider);

    // Método para obtener el primer PO de una organización específica
    Usuario findFirstByRol_IdRolAndOrganizacion_IdOrganizacion(Integer idRol, Integer idOrganizacion);

    // Encontrar usuarios por rol
    List<Usuario> findByRol_NombreRol(String nombreRol);

    // Búsqueda avanzada con múltiples filtros
    @Query("SELECT u FROM Usuario u WHERE " +
            "(:nombre IS NULL OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
            "(:correo IS NULL OR LOWER(u.correo) LIKE LOWER(CONCAT('%', :correo, '%'))) AND " +
            "(:nombreRol IS NULL OR u.rol.nombreRol = :nombreRol) AND " +
            "(:estado IS NULL OR u.estado = :estado)")

    Page<Usuario> findByFiltros(@Param("nombre") String nombre,
                                @Param("correo") String correo,
                                @Param("nombreRol") String nombreRol,
                                @Param("estado") Boolean estado,
                                Pageable pageable);

    // Nuevo para la vista "organizacion" del PO

        // Para obtener usuarios de una organización con sus roles cargados
        @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.rol WHERE u.organizacion.idOrganizacion = :organizacionId")
        List<Usuario> findByOrganizacionIdWithRol(@Param("organizacionId") Integer organizacionId);

    // Nuevo esto es para obtener usuarios por rol (administradores)

        // Buscar usuarios por rol específico (para encontrar administradores)
        List<Usuario> findByRol_IdRol(Integer idRol);

        // Buscar usuarios administradores (asumiendo que idRol 1 o 2 son admins)
        List<Usuario> findByRol_IdRolIn(List<Integer> idsRoles);

        // Buscar usuario con sus organizaciones (para relaciones)
        @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.organizacion WHERE u.dni = :dni")
        Optional<Usuario> findByDniWithOrganizacion(@Param("dni") String dni);

        // Verificar si un usuario pertenece a una organización específica
        @Query("SELECT COUNT(u) > 0 FROM Usuario u WHERE u.dni = :dni AND u.organizacion.idOrganizacion = :idOrganizacion")
        boolean existsByDniAndOrganizacion(@Param("dni") String dni, @Param("idOrganizacion") Integer idOrganizacion);
        // En UsuarioRepository - agregar estos métodos:

        // NUEVO: Buscar por DNI que retorne Optional (sin cambiar el existente)
        Optional<Usuario> findOptionalByDni(String dni);

        // NUEVO: Buscar por DNI o Correo (para verificación flexible)
        @Query("SELECT u FROM Usuario u WHERE u.dni = :dni OR u.correo = :correo")
        Optional<Usuario> findByDniOrCorreo(@Param("dni") String dni, @Param("correo") String correo);

        // NUEVO: Verificar si usuario tiene organización asignada
        @Query("SELECT COUNT(u) > 0 FROM Usuario u WHERE u.dni = :dni AND u.organizacion IS NOT NULL")
        boolean tieneOrganizacionAsignada(@Param("dni") String dni);

        @Query("SELECT u FROM Usuario u WHERE u.organizacion.idOrganizacion = :idOrganizacion AND u.rol.nombreRol = 'PO'")
        Usuario findPoByOrganizacion(@Param("idOrganizacion") Integer idOrganizacion);

        // NUEVO: Contar usuarios activos por organización (para limpieza de organizaciones huérfanas)
        long countByOrganizacionIdOrganizacionAndEstado(Integer idOrganizacion, Boolean estado);

}
