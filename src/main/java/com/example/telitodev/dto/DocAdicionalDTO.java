package com.example.telitodev.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class DocAdicionalDTO {

    private Integer idApi;
    private Integer idVersion;

    private List<MultipartFile> files;
    private List<String> descriptions;
    private List<String> formatos;


    public DocAdicionalDTO() {}

    // Getters y Setters
    public Integer getIdApi() {
        return idApi;
    }
    public void setIdApi(Integer idApi) {
        this.idApi = idApi;
    }

    public Integer getIdVersion() {
        return idVersion;
    }
    public void setIdVersion(Integer idVersion) {
        this.idVersion = idVersion;
    }

    public List<MultipartFile> getFiles() {
        return files;
    }
    public void setFiles(List<MultipartFile> files) {
        this.files = files;
    }

    public List<String> getDescriptions() {
        return descriptions;
    }
    public void setDescriptions(List<String> descriptions) {
        this.descriptions = descriptions;
    }

    public List<String> getFormatos() {
        return formatos;
    }
    public void setFormatos(List<String> formatos) {
        this.formatos = formatos;
    }
}
