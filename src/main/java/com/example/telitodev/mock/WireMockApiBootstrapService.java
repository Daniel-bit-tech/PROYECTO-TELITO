package com.example.telitodev.mock;

import com.example.telitodev.entity.Api;
import com.example.telitodev.entity.Documentacion;
import com.example.telitodev.repository.ApiRepository;
import com.example.telitodev.repository.DocumentacionRepository;
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
            Optional<Documentacion> docOpt = documentacionRepository.findFirstByApi_IdApi(api.getIdApi());
            if (docOpt.isEmpty()) continue;
            String apiName = api.getNombre().replaceAll("\\s+","").toLowerCase();
            String openApiJson = (String) docOpt.get().getContenido();
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