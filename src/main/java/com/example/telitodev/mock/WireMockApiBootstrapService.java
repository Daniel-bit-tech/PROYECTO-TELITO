package com.example.telitodev.mock;

import com.example.telitodev.entity.Api;
import com.example.telitodev.entity.ContratoApi;
import com.example.telitodev.entity.Documentacion;
import com.example.telitodev.repository.ApiRepository;
import com.example.telitodev.repository.ContratoRepository;
import com.example.telitodev.repository.DocumentacionRepository;
import com.example.telitodev.service.S3Services.S3DocsApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

@Service
public class WireMockApiBootstrapService {

    @Autowired private DocumentacionRepository documentacionRepository;
    @Autowired private ApiRepository apiRepository;
    @Autowired private ApiDocParser apiDocParser;
    @Autowired private WireMockDynamicLoader wireMockDynamicLoader;
    @Autowired private ContratoRepository contratoRepository;
    @Autowired private S3DocsApiService s3DocsApiService;


    private final String[] ENVS = {"prod","dev","qa"};

    @PostConstruct
    public void init() {
        try {
            loadAllApis();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadAllApis() throws Exception {

        wireMockDynamicLoader.getWireMockServer().resetAll();
        List<Api> apis = apiRepository.findAll();

        for (Api api : apis) {
            Optional<ContratoApi> contratoOpt = contratoRepository.findLatestByApiId(api.getIdApi());

            if (contratoOpt.isEmpty()) continue;

            ContratoApi contrato = contratoOpt.get();

            String apiName = api.getNombre().replaceAll("\\s+","").toLowerCase();
            String openApiJson;

            if (contrato.getUrlContrato() != null && !contrato.getUrlContrato().isBlank()) {
                try {
                    openApiJson = s3DocsApiService.obtenerContenidoS3(contrato.getUrlContrato());
                    System.out.println("Mock: Contrato S3 cargado para API " + apiName);
                } catch (RuntimeException e) {
                    System.err.println(" Mock: Fallo al cargar S3. Saltando API " + apiName);
                    continue; // Saltar esta API si S3 falla
                }
            } else {
                // CASO 2: Contrato legacy almacenado en la columna 'contenido'
                openApiJson = contrato.getContenido();
                System.out.println(" Mock: Contrato BD cargado para API " + apiName);
            }

            if (openApiJson == null || openApiJson.isEmpty()) continue;


            List<StubDefinition> stubs = apiDocParser.parseOpenApi(openApiJson, apiName);

            for (String env : ENVS) {
                for (StubDefinition sd : stubs) {
                    StubDefinition copy = deepCopy(sd);
                    copy.entorno = env;

                    if ("prod".equals(env)) {
                        if (copy.method == org.springframework.http.HttpMethod.POST && copy.status == 200) copy.status = 201;
                    }

                    wireMockDynamicLoader.registerStub(copy);
                }
            }
        }
    }

    public void reloadApi(Integer apiId) throws Exception {
        Optional<Api> opt = apiRepository.findById(apiId);
        if (opt.isEmpty()) return;
        Api api = opt.get();
        Optional<Documentacion> docOpt = documentacionRepository.findFirstByApi_IdApi(apiId);
        if (docOpt.isEmpty()) return;
        String apiName = api.getNombre().replaceAll("\\s+","").toLowerCase();
        List<StubDefinition> stubs = apiDocParser.parseOpenApi((String) docOpt.get().getContenido(), apiName);
        wireMockDynamicLoader.getWireMockServer().resetAll();
        loadAllApis();
    }

    private StubDefinition deepCopy(StubDefinition sd) {
        StubDefinition c = new StubDefinition();
        c.apiName = sd.apiName; c.path = sd.path; c.method = sd.method;
        c.requiredHeaders = sd.requiredHeaders == null ? null : List.copyOf(sd.requiredHeaders);
        c.requestBodyContains = sd.requestBodyContains; c.status = sd.status; c.body = sd.body;
        return c;
    }
}