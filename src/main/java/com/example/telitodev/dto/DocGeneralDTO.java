package com.example.telitodev.dto;

import com.example.telitodev.entity.Documentacion;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.util.List;

public class DocGeneralDTO {

    // Campos comunes
    private Integer idApi;
    private String nombreApi;
    private Integer idVersion;
    private String numeroVersion;

    // Campos para documentación técnica (tabla: documentacion)
    private List<DocumentacionItemDTO> documentosTecnicos;

    // Campos para documentación de alto nivel (tabla: doc_alto_nivel)
    private Integer idDocAltoNivel;
    @Size(min = 100, max = 400,message = "Describa este campo con entre 100 y 400 caracteres.")
    private String beneficios;
    @Size(min = 100, max = 400,message = "Describa este campo con entre 100 y 400 caracteres.")
    private String limitaciones;
    @Size(min = 100, max = 400,message = "Describa este campo con entre 100 y 400 caracteres.")
    private String flujoFuncional;
    @Size(min = 100, max = 400,message = "Describa este campo con entre 100 y 400 caracteres.")
    private String sla;
    @Size(min = 100, max = 400,message = "Describa este campo con entre 100 y 400 caracteres.")
    private String costos;
    @Size(min = 100, max = 400,message = "Describa este campo con entre 100 y 400 caracteres.")
    private String ejemplosIntegracion;

    // Campos auxiliares para la vista
    private List<MultipartFile> archivosTecnicos;
    private List<String> descripcionesTecnicas;

    // Getters y Setters
    public Integer getIdApi() {
        return idApi;
    }
    public void setIdApi(Integer idApi) {
        this.idApi = idApi;
    }

    public String getNombreApi() {
        return nombreApi;
    }
    public void setNombreApi(String nombreApi) {
        this.nombreApi = nombreApi;
    }

    public String getNumeroVersion() {
        return numeroVersion;
    }
    public void setNumeroVersion(String numeroVersion) {
        this.numeroVersion = numeroVersion;
    }

    public List<DocumentacionItemDTO> getDocumentosTecnicos() {
        return documentosTecnicos;
    }
    public void setDocumentosTecnicos(List<DocumentacionItemDTO> documentosTecnicos) {
        this.documentosTecnicos = documentosTecnicos;
    }

    public Integer getIdVersion() {
        return idVersion;
    }
    public void setIdVersion(Integer idVersion) {
        this.idVersion = idVersion;
    }

    public Integer getIdDocAltoNivel() {
        return idDocAltoNivel;
    }
    public void setIdDocAltoNivel(Integer idDocAltoNivel) {
        this.idDocAltoNivel = idDocAltoNivel;
    }

    public String getBeneficios() {
        return beneficios;
    }
    public void setBeneficios(String beneficios) {
        this.beneficios = beneficios;
    }

    public String getLimitaciones() {
        return limitaciones;
    }
    public void setLimitaciones(String limitaciones) {
        this.limitaciones = limitaciones;
    }

    public String getFlujoFuncional() {
        return flujoFuncional;
    }
    public void setFlujoFuncional(String flujoFuncional) {
        this.flujoFuncional = flujoFuncional;
    }

    public String getSla() {
        return sla;
    }
    public void setSla(String sla) {
        this.sla = sla;
    }

    public String getCostos() {
        return costos;
    }
    public void setCostos(String costos) {
        this.costos = costos;
    }

    public String getEjemplosIntegracion() {
        return ejemplosIntegracion;
    }
    public void setEjemplosIntegracion(String ejemplosIntegracion) {
        this.ejemplosIntegracion = ejemplosIntegracion;
    }

    public List<MultipartFile> getArchivosTecnicos() {
        return archivosTecnicos;
    }
    public void setArchivosTecnicos(List<MultipartFile> archivosTecnicos) {
        this.archivosTecnicos = archivosTecnicos;
    }

    public List<String> getDescripcionesTecnicas() {
        return descripcionesTecnicas;
    }
    public void setDescripcionesTecnicas(List<String> descripcionesTecnicas) {
        this.descripcionesTecnicas = descripcionesTecnicas;
    }

    // DTO para items individuales de documentación técnica
    public class DocumentacionItemDTO {
        private Integer idDocumentacion;
        private String tipo;
        private String urlDocumento;
        private String contenido;
        @Size(min = 10, max = 50,message = "La descripción del archivo debe tener entre 10 y 50 caracteres.")
        private String descripcion;
        private Timestamp fechaModificacion = new Timestamp(System.currentTimeMillis());
        private Integer idAPI;
        private Integer idVersion;
        @NotNull(message = "Debe elegir un formato.")
        private Documentacion.FormatoDoc formato;
        private MultipartFile archivo;

        // Getters y Setters

        public Integer getIdDocumentacion() {
            return idDocumentacion;
        }
        public void setIdDocumentacion(Integer idDocumentacion) {
            this.idDocumentacion = idDocumentacion;
        }

        public String getUrlDocumento() {
            return urlDocumento;
        }
        public void setUrlDocumento(String urlDocumento) {
            this.urlDocumento = urlDocumento;
        }

        public String getTipo() {
            return tipo;
        }
        public void setTipo(String tipo) {
            this.tipo = tipo;
        }

        public String getContenido() {
            return contenido;
        }
        public void setContenido(String contenido) {
            this.contenido = contenido;
        }

        public String getDescripcion() {
            return descripcion;
        }
        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        public Timestamp getFechaModificacion() {
            return fechaModificacion;
        }
        public void setFechaModificacion(Timestamp fechaModificacion) {
            this.fechaModificacion = fechaModificacion;
        }

        public Integer getIdAPI() {
            return idAPI;
        }
        public void setIdAPI(Integer idAPI) {
            this.idAPI = idAPI;
        }

        public Integer getIdVersion() {
            return idVersion;
        }
        public void setIdVersion(Integer idVersion) {
            this.idVersion = idVersion;
        }

        public Documentacion.FormatoDoc getFormato() {
            return formato;
        }
        public void setFormato(Documentacion.FormatoDoc formato) {
            this.formato = formato;
        }

        public MultipartFile getArchivo() {
            return archivo;
        }
        public void setArchivo(MultipartFile archivo) {
            this.archivo = archivo;
        }
    }

}
