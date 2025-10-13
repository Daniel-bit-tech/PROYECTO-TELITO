package com.example.telitodev.service;

import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> obtenerTodosUsuarios() {
        return usuarioRepository.findAll();
    }

    public Usuario findByCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo);
    }

    public Usuario save(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    // Estos services son para las validaciones en SolAccesoOrgService

    public Optional<Usuario> obtenerUsuarioPorDni(String dni) {
        return Optional.ofNullable(usuarioRepository.findByDni(dni));
    }

    public boolean existeUsuarioPorDni(String dni) {
        return usuarioRepository.existsByDni(dni);
    }

    public List<Usuario> obtenerUsuariosPorRol(Integer idRol) {
        return usuarioRepository.findByRol_IdRol(idRol);
    }

    public List<Usuario> obtenerAdministradores() {
        // Asumiendo que los roles 1 y 2 son administradores
        return usuarioRepository.findByRol_IdRolIn(List.of(1, 2));
    }

    public Optional<Usuario> obtenerUsuarioConOrganizacion(String dni) {
        return usuarioRepository.findByDniWithOrganizacion(dni);
    }

    public boolean usuarioPerteneceAOrganizacion(String dni, Integer idOrganizacion) {
        return usuarioRepository.existsByDniAndOrganizacion(dni, idOrganizacion);
    }

    // En UsuarioService - agregar estos métodos:

    /**
     * NUEVO: Buscar usuario por DNI que retorna Optional
     */
    public Optional<Usuario> findOptionalByDni(String dni) {
        return usuarioRepository.findOptionalByDni(dni);
    }

    /**
     * NUEVO: Verificar si usuario tiene organización asignada
     */
    public boolean tieneOrganizacionAsignada(String dni) {
        return usuarioRepository.tieneOrganizacionAsignada(dni);
    }

    /**
     * NUEVO: Buscar por DNI o correo
     */
    public Optional<Usuario> findByDniOrCorreo(String dni, String correo) {
        return usuarioRepository.findByDniOrCorreo(dni, correo);
    }

}
