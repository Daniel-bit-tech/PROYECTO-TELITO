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

        System.out.println("🔍 Intentando autenticar con: " + correo);
        
        // Buscar usuario por correo personal o correo corporativo
        Usuario usuario = usuarioRepository.findByCorreo(correo);
        
        // Si no se encuentra por correo personal, buscar por correo corporativo
        if (usuario == null) {
            System.out.println("❌ No encontrado por correo personal, buscando por correo corporativo...");
            usuario = usuarioRepository.findByCorreoCorporativo(correo);
            if (usuario != null) {
                System.out.println("✅ Usuario encontrado por correo corporativo");
                System.out.println("   - DNI: " + usuario.getDni());
                System.out.println("   - Nombre: " + usuario.getNombre());
                System.out.println("   - Correo personal: " + usuario.getCorreo());
                System.out.println("   - Correo corporativo: " + usuario.getCorreoCorporativo());
                System.out.println("   - Tiene contraseña: " + (usuario.getContrasena() != null && !usuario.getContrasena().isEmpty()));
                
                // Mostrar hash de contraseña para debugging
                if (usuario.getContrasena() != null) {
                    String hash = usuario.getContrasena();
                    System.out.println("   - Hash contraseña (primeros 60 chars): " + (hash.length() > 60 ? hash.substring(0, 60) + "..." : hash));
                    System.out.println("   - Longitud del hash: " + hash.length());
                    System.out.println("   - Comienza con $2a, $2b o $2y: " + (hash.startsWith("$2a") || hash.startsWith("$2b") || hash.startsWith("$2y")));
                }
            }
        } else {
            System.out.println("✅ Usuario encontrado por correo personal");
            System.out.println("   - DNI: " + usuario.getDni());
            System.out.println("   - Nombre: " + usuario.getNombre());
            System.out.println("   - Tiene contraseña: " + (usuario.getContrasena() != null && !usuario.getContrasena().isEmpty()));
        }

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
        System.out.println("Usuario activo encontrado: " + correo + " - Rol: " + nombreRol);

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + nombreRol));
        if(usuario.getCorreo().equals("dev@gmail.com")) {
            authorities.add(new SimpleGrantedAuthority("ROLE_DEVINT"));
        }

        // Usar el correo con el que el usuario intentó iniciar sesión (puede ser personal o corporativo)
        return User.withUsername(correo)
                .password(usuario.getContrasena())
                .disabled(false) // Ya verificamos que está activo
                .authorities(authorities)
                .build();
    }
}
