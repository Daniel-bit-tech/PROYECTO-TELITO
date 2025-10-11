package com.example.telitodev.dto;


import java.util.Map;

public class SandboxResponseDto {
    private int status;
    private long time;
    private String body;
    private Map<String, String> headers;


    public SandboxResponseDto(int status, long time, String body, Map<String, String> headers) {
        this.status = status;
        this.time = time;
        this.body = body;
        this.headers = headers;
    }


    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}