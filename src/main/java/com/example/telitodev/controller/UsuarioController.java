package com.example.telitodev.controller;

import com.example.telitodev.entity.Usuario;
import com.example.telitodev.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuario")
public class UsuarioController {


    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioService usuarioService, PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
    }





    @GetMapping("/cambiar-password")
    public String showPasswordChangeForm(Model model, Authentication authentication) {

        String correoUsuario = authentication.getName();


        Usuario usuario = usuarioService.findByCorreo(correoUsuario);


        model.addAttribute("usuario", usuario);


        return "desarrollador/cambiar-password";
    }

    // Método para procesar el cambio de contraseña
    @PostMapping("/cambiar-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttrs) {

        String correoUsuario = authentication.getName();
        Usuario usuario = usuarioService.findByCorreo(correoUsuario);

        // 1. Validar que la contraseña actual sea correcta
        if (!passwordEncoder.matches(currentPassword, usuario.getContrasena())) {
            redirectAttrs.addFlashAttribute("error", "La contraseña actual es incorrecta.");
            return "redirect:/desarrollador/cambiar-password";
        }


        if (!newPassword.equals(confirmPassword)) {
            redirectAttrs.addFlashAttribute("error", "Las nuevas contraseñas no coinciden.");
            return "redirect:/desarrollador/cambiar-password";
        }


        String hashedNewPassword = passwordEncoder.encode(newPassword);
        usuario.setContrasena(hashedNewPassword);
        usuarioService.save(usuario);

        redirectAttrs.addFlashAttribute("success", "Contraseña cambiada exitosamente.");
        return "redirect:/perfil";
    }



    @GetMapping("/desarrollador/perfil")
    public String mostrarPerfilUsuario(Model model, Authentication authentication) {

        String correoUsuario = authentication.getName();


        Usuario usuario = usuarioService.findByCorreo(correoUsuario);


        model.addAttribute("usuario", usuario);


        return "desarrollador/perfil";
    }


}