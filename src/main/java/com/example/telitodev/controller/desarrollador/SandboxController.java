package com.example.telitodev.controller.desarrollador;


import com.example.telitodev.dto.EntornoDto;
import com.example.telitodev.dto.SandboxApiDetailsDto;
import com.example.telitodev.dto.SandboxRequestDto;
import com.example.telitodev.dto.SandboxResponseDto;
import com.example.telitodev.entity.ApiHasEntorno;
import com.example.telitodev.entity.Usuario;
import com.example.telitodev.repository.ApiHasEntornoRepository;
import com.example.telitodev.repository.UsuarioRepository;
import com.example.telitodev.service.SandboxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sandbox")
public class SandboxController {

    @Autowired
    private SandboxService sandboxService;
    @Autowired
    private UsuarioRepository usuarioRepository;


    @GetMapping("/api-details/{id}")
    public ResponseEntity<SandboxApiDetailsDto> getApiDetailsById(@PathVariable Integer id, Authentication authentication) {

        String userEmail = authentication.getName();


        Usuario usuario = usuarioRepository.findByCorreo(userEmail);
        if (usuario == null) {
            throw new RuntimeException("Usuario no encontrado con el email: " + userEmail);
        }
        String userDni = usuario.getDni();


        System.out.println("### USUARIO LOGUEADO (EMAIL): " + userEmail + ", DNI OBTENIDO: " + userDni);


        SandboxApiDetailsDto details = sandboxService.getApiDetails(id, userDni);

        return ResponseEntity.ok(details);
    }

    @PostMapping("/execute")
    public ResponseEntity<SandboxResponseDto> executeSandboxRequest(@RequestBody SandboxRequestDto requestDto, Authentication authentication) {


        String userDni = authentication.getName();

        SandboxResponseDto response = sandboxService.executeRequest(requestDto, userDni);

        return ResponseEntity.ok(response);
    }




}

