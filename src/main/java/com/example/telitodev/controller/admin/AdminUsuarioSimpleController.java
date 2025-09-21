package com.example.telitodev.controller.admin;

import com.example.telitodev.entity.Usuario;
import com.example.telitodev.entity.Rol;
import com.example.telitodev.repository.UsuarioRepository;
import com.example.telitodev.repository.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/usuarios-simple")
public class AdminUsuarioSimpleController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    /**
     * Página principal de gestión de usuarios (versión simplificada)
     */
    @GetMapping
    public String index(Model model) {
        try {
            // Obtener todos los usuarios
            List<Usuario> usuarios = usuarioRepository.findAll();
            
            // Obtener todos los roles para el filtro
            List<Rol> roles = rolRepository.findAll();

            // Estadísticas básicas
            long totalUsuarios = usuarioRepository.count();
            long usuariosActivos = 0;
            long usuariosInactivos = 0;
            
            for (Usuario u : usuarios) {
                if (u.getEstado()) {
                    usuariosActivos++;
                } else {
                    usuariosInactivos++;
                }
            }

            // Agregar datos al modelo
            model.addAttribute("usuarios", usuarios);
            model.addAttribute("roles", roles);
            model.addAttribute("totalUsuarios", totalUsuarios);
            model.addAttribute("usuariosActivos", usuariosActivos);
            model.addAttribute("usuariosInactivos", usuariosInactivos);

            System.out.println("=== DEBUG USUARIOS SIMPLE ===");
            System.out.println("Total usuarios encontrados: " + usuarios.size());
            for (Usuario u : usuarios) {
                System.out.println("Usuario: " + u.getNombre() + " " + u.getApellidoPaterno() + 
                    " - Correo: " + u.getCorreo() + 
                    " - Rol: " + (u.getRol() != null ? u.getRol().getNombreRol() : "Sin rol") +
                    " - Estado: " + u.getEstado());
            }

            return "admin/usuarios-simple-v2";

        } catch (Exception e) {
            System.err.println("Error en gestión de usuarios simple: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar usuarios: " + e.getMessage());
            return "error";
        }
    }
}