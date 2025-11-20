package com.example.telitodev.mock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ApiDocParser {

    private final ObjectMapper objectMapper;

    private static final Set<String> HTTP_METHODS = Set.of("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS", "TRACE");

    public ApiDocParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<StubDefinition> parseOpenApi(String openApiJson, String apiName) throws Exception {
        JsonNode root = objectMapper.readTree(openApiJson);
        JsonNode paths = root.path("paths");

        List<StubDefinition> list = new ArrayList<>();

        if (paths.isMissingNode()) {
            System.err.println(" No se encontró la sección 'paths' en el JSON de " + apiName);
            return list;
        }

        Iterator<Map.Entry<String, JsonNode>> pathsIter = paths.fields();
        while (pathsIter.hasNext()) {
            Map.Entry<String, JsonNode> entry = pathsIter.next();
            String path = entry.getKey();
            JsonNode methodsNode = entry.getValue();
            System.out.println("DEBUG PARSER: Leyendo Path: " + path);

            Iterator<Map.Entry<String, JsonNode>> methodsIter = methodsNode.fields();
            while (methodsIter.hasNext()) {
                Map.Entry<String, JsonNode> m = methodsIter.next();
                String key = m.getKey().toUpperCase();

                if (!HTTP_METHODS.contains(key)) {
                    continue;
                }

                JsonNode methodNode = m.getValue();
                String methodStr = key;
                System.out.println("DEBUG PARSER: Intentando Método: " + methodStr.toUpperCase() + " en " + path);

                int status = 200;
                String responseBody = "{\"message\":\"stub " + apiName + " " + path + "\"}";

                JsonNode responses = methodNode.path("responses");
                Iterator<Map.Entry<String, JsonNode>> respIter = responses.fields();
                if (respIter.hasNext()) {
                    Map.Entry<String, JsonNode> r = respIter.next();
                    try {
                        status = Integer.parseInt(r.getKey());
                    } catch (NumberFormatException ex) {
                        status = 200;
                    }

                    JsonNode content = r.getValue().path("content");
                    if (!content.isMissingNode() && content.has("application/json")) {
                        JsonNode schemaOrExample = content.get("application/json");
                        if (schemaOrExample.has("example")) {
                            responseBody = schemaOrExample.get("example").toString();
                        }
                    }
                }

                StubDefinition sd = new StubDefinition();
                sd.apiName = apiName;
                sd.path = path;
                sd.method = HttpMethod.valueOf(methodStr);
                sd.status = status;
                sd.body = responseBody;
                sd.requiredHeaders = new ArrayList<>();

                if (methodNode.has("requestBody") && methodNode.get("requestBody").path("required").asBoolean(false)) {

                    sd.requestBodyContains = null;
                } else {
                    sd.requestBodyContains = null;
                }
                list.add(sd);
            }
        }
        return list;
    }
}