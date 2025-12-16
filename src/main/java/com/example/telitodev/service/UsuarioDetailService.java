package com.example.telitodev.service;

import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.UsuarioRepository;
import com.example.telitodev.exception.UsuarioDesactivadoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioDetailService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {

        // Buscar usuario por correo sin filtrar por estado
        Usuario usuario = usuarioRepository.findByCorreo(correo);

        if (usuario == null) {
            System.err.println("Usuario no encontrado con correo: " + correo);
            throw new UsernameNotFoundException("Credenciales inválidas");
        }

        // Verificar si es usuario interno (debe tener contraseña)
        if (usuario.isUsuarioInterno() && !usuario.hasPassword()) {
            System.err.println("Usuario interno sin contraseña: " + correo);
            throw new UsernameNotFoundException("Credenciales inválidas");
        }

        // Verificar si el usuario está desactivado
        if (!usuario.getEstado()) {
            System.err.println("🚫 USUARIO DESACTIVADO DETECTADO:");
            System.err.println("   - Email: " + correo);
            System.err.println("   - Estado: " + usuario.getEstado());
            System.err.println("   - Lanzando UsuarioDesactivadoException");
            throw new UsuarioDesactivadoException("Usuario inactivo. Comuníquese con el administrador.");
        }

        // Verificar si el usuario tiene rol asignado
        if (usuario.getRol() == null) {
            throw new UsernameNotFoundException("Usuario sin rol asignado");
        }

        String nombreRol = usuario.getRol().getNombreRol();
        System.out.println("Usuario activo encontrado: " + usuario.getCorreo() + " - Rol: " + nombreRol);

        // DEBUG: Verificar hash de contraseña
        String passwordHash = usuario.getContrasena();
        System.out.println("🔐 DEBUG PASSWORD HASH:");
        System.out.println("   - Hash length: " + (passwordHash != null ? passwordHash.length() : "null"));
        System.out.println("   - Hash preview: "
                + (passwordHash != null ? passwordHash.substring(0, Math.min(20, passwordHash.length())) + "..."
                        : "null"));
        System.out.println("   - Hash ends with: "
                + (passwordHash != null && passwordHash.length() > 3 ? passwordHash.substring(passwordHash.length() - 3)
                        : "N/A"));

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + nombreRol));
        if (usuario.getEquipo() != null && usuario.getOrganizacion() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_DEVINT"));
        }

        return User.withUsername(usuario.getCorreo())
                .password(passwordHash)
                .disabled(false) // Ya verificamos que está activo
                .authorities(authorities)
                .build();
    }
}
