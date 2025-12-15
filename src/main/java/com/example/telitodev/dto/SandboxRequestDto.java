package com.example.telitodev.dto;

import java.util.Map;

public class SandboxRequestDto {
    private Integer apiId;
    private String method;
    private String targetUrl;
    private Map<String, String> headers;
    private Object body;
    private String apiKey;
    private Integer environmentId;
    private String endpoint;

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public Integer getApiId() { return apiId; }
    public void setApiId(Integer apiId) { this.apiId = apiId; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getTargetUrl() { return targetUrl; }
    public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }
    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public Integer getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(Integer environmentId) {
        this.environmentId = environmentId;
    }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
}