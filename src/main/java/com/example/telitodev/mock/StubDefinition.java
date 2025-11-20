package com.example.telitodev.mock;

import org.springframework.http.HttpMethod;
import java.util.List;
import java.util.Map;

public class StubDefinition {
    public String entorno;
    public String apiName;
    public String path;
    public HttpMethod method;
    public List<String> requiredHeaders;
    public String requestBodyContains;
    public int status;
    public String body;

    public String getEntorno() {
        return entorno;
    }

    public void setEntorno(String entorno) {
        this.entorno = entorno;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public List<String> getRequiredHeaders() {
        return requiredHeaders;
    }

    public void setRequiredHeaders(List<String> requiredHeaders) {
        this.requiredHeaders = requiredHeaders;
    }

    public String getRequestBodyContains() {
        return requestBodyContains;
    }

    public void setRequestBodyContains(String requestBodyContains) {
        this.requestBodyContains = requestBodyContains;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
