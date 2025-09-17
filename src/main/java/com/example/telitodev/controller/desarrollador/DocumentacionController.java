package com.example.telitodev.controller.desarrollador;


import com.example.telitodev.entity.Documentacion;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.DocumentacionRepository;
import com.example.telitodev.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/documentacion")
public class DocumentacionController {

    final UsuarioRepository usuarioRepository;
    final DocumentacionRepository documentacionRepository;

    public DocumentacionController(UsuarioRepository usuarioRepository, DocumentacionRepository documentacionRepository) {
        this.usuarioRepository = usuarioRepository;
        this.documentacionRepository = documentacionRepository;
    }


    @GetMapping("/{idDoc}")
    public String showLoginForm(@PathVariable Integer idDoc, Model model, Authentication auth) {


        Documentacion doc = documentacionRepository.getReferenceById(idDoc);


        Usuario usuario = usuarioRepository.findByCorreo(auth.getName());
        model.addAttribute("usuario", usuario);

        return "desarrollador/documentacion";
    }


}
