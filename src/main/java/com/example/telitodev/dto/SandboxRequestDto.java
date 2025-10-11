package com.example.telitodev.dto;

import java.util.Map;

public class SandboxRequestDto {
    private Integer apiId;
    private String method;
    private String targetUrl;
    private Map<String, String> headers;
    private String body;


    public Integer getApiId() { return apiId; }
    public void setApiId(Integer apiId) { this.apiId = apiId; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getTargetUrl() { return targetUrl; }
    public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }
    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
}