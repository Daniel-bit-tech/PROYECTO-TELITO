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

    private static final Set<String> HTTP_METHODS = Set.of("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS",
            "TRACE");

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
                    // System.out.println(content);
                    if (!content.isMissingNode() && content.has("application/json")) {
                        JsonNode mediaType = content.get("application/json");
                        if (mediaType.has("example")) {
                            System.out.println("Se responde desde example");
                            responseBody = mediaType.get("example").toString();
                        } else if (mediaType.has("schema")) {
                            // Intentar generar dummy data desde el schema
                            try {
                                System.out.println("Se genera data dummy desde el schema");
                                JsonNode schema = mediaType.get("schema");
                                responseBody = generateDummyFromSchema(schema, root).toString();
                            } catch (Exception e) {
                                System.err.println("Error generando dummy data desde schema: " + e.getMessage());
                            }
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

    private JsonNode generateDummyFromSchema(JsonNode schema, JsonNode root) {
        if (schema.has("type")) {
            String type = schema.get("type").asText();
            switch (type) {
                case "string":
                    if (schema.has("format") && "date-time".equals(schema.get("format").asText())) {
                        return objectMapper.valueToTree("2025-12-14T10:00:00Z");
                    }
                    if (schema.has("enum")) {
                        return schema.get("enum").get(0);
                    }
                    return objectMapper.valueToTree("string_value");
                case "integer":
                case "number":
                    return objectMapper.valueToTree(123);
                case "boolean":
                    return objectMapper.valueToTree(true);
                case "array":
                    if (schema.has("items")) {
                        JsonNode item = generateDummyFromSchema(schema.get("items"), root);
                        return objectMapper.createArrayNode().add(item);
                    }
                    return objectMapper.createArrayNode();
                case "object":
                    if (schema.has("properties")) {
                        var objectNode = objectMapper.createObjectNode();
                        schema.get("properties").fields().forEachRemaining(entry -> {
                            objectNode.set(entry.getKey(), generateDummyFromSchema(entry.getValue(), root));
                        });
                        return objectNode;
                    }
                    return objectMapper.createObjectNode();
            }
        } else if (schema.has("$ref")) {
            String ref = schema.get("$ref").asText();

            if (ref.startsWith("#/")) {
                String jsonPointer = ref.substring(1);
                JsonNode resolvedSchema = root.at(jsonPointer);
                if (!resolvedSchema.isMissingNode()) {
                    return generateDummyFromSchema(resolvedSchema, root);
                }
            }
            return objectMapper.valueToTree("Ref: " + ref); // Fallback si no resuelve
        }
        return objectMapper.valueToTree("unknown");
    }
}