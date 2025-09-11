package com.example.telitodev.service;

import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.UsuarioRepository;
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

        Optional<Usuario> optUsuario = usuarioRepository.findByCorreoAndEstado(correo, true);

        if (optUsuario.isEmpty()) {
            System.err.println("Usuario no encontrado");
            throw new UsernameNotFoundException("Usuario no encontrado: " + correo);
        } else {
            Usuario usuario = optUsuario.get();
            String nombreRol = usuario.getRol().getNombreRol();

            System.out.println("Usuario encontrado "+usuario.getCorreo()+" "+nombreRol);


            return User.withUsername(usuario.getCorreo())
                    .password(usuario.getContrasena())
                    .disabled(!usuario.getEstado())
                    .authorities(new SimpleGrantedAuthority("ROLE_"+nombreRol))
                    .build();
        }
    }
}
