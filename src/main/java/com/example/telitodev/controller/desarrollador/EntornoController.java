package com.example.telitodev.controller.desarrollador;


import com.example.telitodev.dto.EntornoDto;
import com.example.telitodev.entity.ApiHasEntorno;
import com.example.telitodev.repository.ApiHasEntornoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class EntornoController {

    @Autowired
    private ApiHasEntornoRepository apiHasEntornoRepository;

    @GetMapping("/apis/{id}/entornos")
    public ResponseEntity<List<EntornoDto>> getEntornosPorApi(@PathVariable Integer id) {
        List<ApiHasEntorno> entornosRel = apiHasEntornoRepository.findByApi_IdApi(id);

        List<EntornoDto> entornosDto = entornosRel.stream()
                .filter(rel -> !rel.getEntorno().getNombre().equalsIgnoreCase("QA"))
                .map(rel -> new EntornoDto(rel.getEntorno().getNombre()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(entornosDto);
    }
}