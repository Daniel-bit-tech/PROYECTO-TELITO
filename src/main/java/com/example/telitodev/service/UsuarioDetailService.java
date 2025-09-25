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
        
        // Verificar si el usuario está desactivado
        if (!usuario.getEstado()) {
            System.err.println("🚫 USUARIO DESACTIVADO DETECTADO:");
            System.err.println("   - Email: " + correo);
            System.err.println("   - Estado: " + usuario.getEstado());
            System.err.println("   - Lanzando UsuarioDesactivadoException");
            throw new UsuarioDesactivadoException("Usuario inactivo. Comuníquese con el administrador.");
        }

        String nombreRol = usuario.getRol().getNombreRol();
        System.out.println("Usuario activo encontrado: " + usuario.getCorreo() + " - Rol: " + nombreRol);

        return User.withUsername(usuario.getCorreo())
                .password(usuario.getContrasena())
                .disabled(false) // Ya verificamos que está activo
                .authorities(new SimpleGrantedAuthority("ROLE_" + nombreRol))
                .build();
    }
}
