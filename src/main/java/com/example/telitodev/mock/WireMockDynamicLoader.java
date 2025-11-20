package com.example.telitodev.mock;


import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Service
public class WireMockDynamicLoader {

    private final WireMockServer wireMockServer;

    public WireMockDynamicLoader(WireMockServer wireMockServer) {
        this.wireMockServer = wireMockServer;
    }
    public WireMockServer getWireMockServer() {
        return wireMockServer;
    }


    public void registerStub(StubDefinition sd) {
        String pathTemplate = sd.path.replaceAll("\\{[^/]+\\}", "([^/]+)");

        String fullPattern = "/mock/" + sd.entorno + "/" + sd.apiName + pathTemplate;
        System.out.println(" WIREMOCK REGISTRADO: " + sd.method + " EN EL PATRÓN: " + fullPattern);


        com.github.tomakehurst.wiremock.client.MappingBuilder builder;
        if (sd.method == HttpMethod.GET) builder = get(urlPathMatching(fullPattern));
        else if (sd.method == HttpMethod.POST) builder = post(urlPathMatching(fullPattern));
        else if (sd.method == HttpMethod.PUT) builder = put(urlPathMatching(fullPattern));
        else if (sd.method == HttpMethod.DELETE) builder = delete(urlPathMatching(fullPattern));
        else builder = any(urlPathMatching(fullPattern));

        if (sd.requiredHeaders != null) {
            for (String h : sd.requiredHeaders) {
                builder.withHeader(h, matching(".+"));
            }
        }

        if (sd.requestBodyContains != null && !sd.requestBodyContains.isEmpty()) {

            builder.withRequestBody(matchingJsonPath("$.*"));
        }
        // -----------------------------------------------------


        com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder response = aResponse()
                .withStatus(sd.status)
                .withHeader("Content-Type", "application/json")
                .withBody(sd.body);

        wireMockServer.stubFor(builder.willReturn(response));
    }

    public void clearStubsForApiAndEnv(String apiName, String entorno) {


    }
}
