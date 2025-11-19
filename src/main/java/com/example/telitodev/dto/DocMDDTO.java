package com.example.telitodev.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class DocMDDTO {

    private Integer idDoc;

    @NotNull(message = "No se puede obtener la Api.")
    private Integer idApi;

    @NotNull(message = "Debe enviar un archivo")
    private MultipartFile mdFile;

    private String descricion;



    //Get y Set
    public MultipartFile getMdFile() {
        return mdFile;
    }
    public void setMdFile(MultipartFile mdFile) {
        this.mdFile = mdFile;
    }

    public Integer getIdApi() {
        return idApi;
    }
    public void setIdApi(Integer idApi) {
        this.idApi = idApi;
    }

    public String getDescricion() {
        return descricion;
    }
    public void setDescricion(String descricion) {
        this.descricion = descricion;
    }

    public Integer getIdDoc() {
        return idDoc;
    }
    public void setIdDoc(Integer idDoc) {
        this.idDoc = idDoc;
    }
}
