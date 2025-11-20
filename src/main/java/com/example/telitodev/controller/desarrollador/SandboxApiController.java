package com.example.telitodev.controller.desarrollador;

import com.example.telitodev.entity.*;
import com.example.telitodev.mock.ApiDocParser;
import com.example.telitodev.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sandbox")
public class SandboxApiController {

    @Autowired private ApiDocParser apiDocParser;
    @Autowired private ApiRepository apiRepository;
    @Autowired private DocumentacionRepository documentacionRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    @Autowired private ApiHasEntornoRepository apiHasEntornoRepository;
    @Autowired private EntornoRepository entornoRepository;

    @PostMapping("/save-active-environment")
    @Transactional
    public ResponseEntity<String> saveActiveEnvironment(
            @RequestParam("apiId") Integer apiId,
            @RequestParam("envId") Integer newEnvId,
            Authentication authentication) {

        final int PROD_ID = 1;
        final int DEV_ID  = 2;
        final int QA_ID   = 3;

        String userEmail = authentication.getName();
        Usuario usuario = usuarioRepository.findByCorreo(userEmail);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Usuario no autenticado.");
        }

        boolean isDeveloper = usuario.getRol().getNombreRol().toUpperCase().contains("DEV");
        if (!isDeveloper) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Solo desarrolladores pueden cambiar entornos.");
        }

        Optional<ApiHasEntorno> currentActiveOpt =
                apiHasEntornoRepository.findFirstByApi_IdApiAndEstado(
                        apiId, ApiHasEntorno.EstadoApiEntorno.Activo
                );

        int currentEnvId = currentActiveOpt
                .map(a -> a.getEntorno().getIdEntorno())
                .orElse(0);

        if (currentEnvId == newEnvId) {
            return ResponseEntity.ok("El entorno ya estaba activo.");
        }

        String error = null;

        if (currentEnvId == PROD_ID) {
            error = "No puedes retroceder desde Producción.";
        } else if (currentEnvId == DEV_ID && newEnvId != QA_ID) {
            error = "De Desarrollo solo puedes ir a QA.";
        } else if (currentEnvId == QA_ID && (newEnvId != PROD_ID && newEnvId != DEV_ID)) {
            error = "De QA solo puedes ir a Desarrollo o Producción.";
        } else if (currentEnvId == 0 && newEnvId == PROD_ID) {
            error = "No puedes iniciar directamente en Producción.";
        }

        if (error != null) {
            return ResponseEntity.badRequest().body(error);
        }

        try {
            Entorno nuevoEntorno = entornoRepository.findById(newEnvId)
                    .orElseThrow(() -> new RuntimeException("Entorno no encontrado con ID: " + newEnvId));

            Api api = apiRepository.findById(apiId)
                    .orElseThrow(() -> new RuntimeException(" API no encontrada."));

            if (currentActiveOpt.isPresent()) {
                ApiHasEntorno registroActual = currentActiveOpt.get();
                registroActual.setEstado(ApiHasEntorno.EstadoApiEntorno.Inactivo);
                apiHasEntornoRepository.save(registroActual);
            }


            Optional<ApiHasEntorno> nuevoRegistroOpt =
                    apiHasEntornoRepository.findByApi_IdApiAndEntorno_IdEntorno(apiId, newEnvId);

            ApiHasEntorno registroNuevo;

            if (nuevoRegistroOpt.isPresent()) {

                registroNuevo = nuevoRegistroOpt.get();
            } else {

                registroNuevo = new ApiHasEntorno();


                registroNuevo.setApi(api);
                registroNuevo.setEntorno(nuevoEntorno);
                ApiHasEntornoId pk = new ApiHasEntornoId();
                pk.setIdApi(apiId);
                pk.setIdEntorno(newEnvId);

                registroNuevo.setId(pk);
            }

            // C. Activar y guardar
            registroNuevo.setEstado(ApiHasEntorno.EstadoApiEntorno.Activo);
            apiHasEntornoRepository.save(registroNuevo);

            return ResponseEntity.ok(" Entorno cambiado a: " + nuevoEntorno.getNombre());

        } catch (Exception e) {
            e.printStackTrace(); // Ver logs para detalle del error 500
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno: " + e.getMessage());
        }
    }

    // --- OTROS ENDPOINTS ---

    @GetMapping("/list-available")
    public ResponseEntity<List<Map<String, Object>>> getAvailableApis(Authentication authentication) {
        String userEmail = authentication.getName();
        Usuario usuario = usuarioRepository.findByCorreo(userEmail);

        if (usuario == null || usuario.getOrganizacion() == null) {
            List<Api> apisPublicas = apiRepository.findByDominio_IdDominio(11);
            return buildApiResponse(apisPublicas);
        }

        Integer idOrganizacionUsuario = usuario.getOrganizacion().getIdOrganizacion();
        List<Api> apisDisponibles = apiRepository.findApisByOrgProjectsAndPublic(idOrganizacionUsuario);

        return buildApiResponse(apisDisponibles);
    }

    @GetMapping("/{apiId}/endpoints")
    public ResponseEntity<List<String>> getAvailableEndpoints(@PathVariable Integer apiId) {
        return documentacionRepository.findFirstByApi_IdApi(apiId)
                .map(doc -> {
                    try {
                        String apiName = apiRepository.findById(apiId)
                                .orElseThrow(() -> new RuntimeException("API no encontrada")).getNombre()
                                .replaceAll("\\s+","").toLowerCase();

                        String openApiJson = (String) doc.getContenido();

                        return ResponseEntity.ok(apiDocParser.parseOpenApi(openApiJson, apiName).stream()
                                .map(stub -> stub.method.name() + " " + stub.path)
                                .collect(Collectors.toList()));
                    } catch (Exception e) {
                        return ResponseEntity.internalServerError().body(List.of("ERROR PARSING DOC"));
                    }
                })
                .orElse(ResponseEntity.ok(List.of()));
    }

    @GetMapping("/{apiId}/entorno/{envId}/url-base-info")
    public ResponseEntity<Map<String, String>> getBaseUrl(@PathVariable Integer apiId, @PathVariable String envId) {
        String envName;
        switch (envId) {
            case "1": envName = "prod"; break;
            case "2": envName = "dev"; break;
            case "3": envName = "qa"; break;
            default: return ResponseEntity.badRequest().build();
        }

        return apiRepository.findById(apiId).map(api -> {
            String apiName = api.getNombre().replaceAll("\\s+","").toLowerCase();
            String urlBase = "http://localhost:8083/mock/" + envName + "/" + apiName;
            return ResponseEntity.ok(Map.of("urlBase", urlBase));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{apiId}/active-environment")
    public ResponseEntity<Map<String, Object>> getActiveEnvironment(@PathVariable Integer apiId) {
        Optional<ApiHasEntorno> activeOpt = apiHasEntornoRepository.findFirstByApi_IdApiAndEstado(
                apiId, ApiHasEntorno.EstadoApiEntorno.Activo);

        if (activeOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of("activeEnvId", 0));
        }
        return ResponseEntity.ok(Map.of("activeEnvId", activeOpt.get().getEntorno().getIdEntorno()));
    }

    private ResponseEntity<List<Map<String, Object>>> buildApiResponse(List<Api> apis) {
        List<Map<String, Object>> response = apis.stream().map(api -> {
            Map<String, Object> map = new HashMap<>();
            map.put("idApi", api.getIdApi());
            map.put("nombre", api.getNombre());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
