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


}
