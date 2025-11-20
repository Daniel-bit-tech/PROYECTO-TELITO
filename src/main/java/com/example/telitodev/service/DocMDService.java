package com.example.telitodev.service;

import com.example.telitodev.entity.Documentacion;
import com.example.telitodev.repository.DocumentacionRepository;
import com.example.telitodev.service.S3Services.S3DocsApiService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class DocMDService {

    private final Parser parser;
    private final HtmlRenderer renderer;
    private final DocumentacionRepository documentacionRepository;
    private final S3DocsApiService s3DocsApiService;

    public DocMDService(DocumentacionRepository documentacionRepository, S3DocsApiService s3DocsApiService) {
        this.documentacionRepository = documentacionRepository;
        MutableDataSet options = new MutableDataSet();

        // Activar extensiones útiles
        options.set(Parser.EXTENSIONS, Arrays.asList(
                TablesExtension.create(),        // tablas
                AutolinkExtension.create()      // links automáticos
//                StrikethroughExtension.create(), // tachado
//                TaskListExtension.create(),      // listas de tareas
//                FencedCodeBlockExtension.create()// bloques de código
        ));

        // Inicializar atributos de clase (no variables locales)
        this.parser = Parser.builder(options).build();
        this.renderer = HtmlRenderer.builder(options).build();
        this.s3DocsApiService = s3DocsApiService;
    }

    public Map<String, String> mapeoSecsDoc(Integer idApi) throws IOException {
        // Obtener la documentación desde la base de datos
        Documentacion doc = documentacionRepository.findByApi_IdApiAndFormatoAndDescripcion(idApi, Documentacion.FormatoDoc.MARKDOWN, "Documentación Técnica");

        if (doc == null || doc.getUrlDocumento() == null) {
            return Collections.emptyMap();
        }

        String md;
        try {
            md = s3DocsApiService.descargarArchivoDesdeS3ComoString(doc.getUrlDocumento());
        } catch (Exception e) {
            throw new IOException("Error al descargar archivo desde S3: " + doc.getDescripcion(), e);
        }

        if (md.trim().isEmpty()) return Collections.emptyMap();

        // Dividir por secciones (## Título)
        Map<String, String> sections = new LinkedHashMap<>();
        String[] parts = md.split("(?m)^## ");

        for (String part : parts) {
            if (part.isBlank() || part.startsWith("# ")) continue;
            String[] lines = part.split("\n", 2);
            String title = lines[0].trim();
            String body = lines.length > 1 ? lines[1] : "";

            Node document = parser.parse("## " + title + "\n" + body);
            String html = renderer.render(document);

            sections.put(title, html);
        }
        return sections;
    }

    // Solo los nombres de sección (para sidebar)
    public List<String> nombresSecsReadme(Integer idApi) {

        Documentacion docReadme = documentacionRepository.findByApi_IdApiAndFormatoAndDescripcion(idApi, Documentacion.FormatoDoc.MARKDOWN,"Documentación Técnica");

        if (docReadme == null || docReadme.getContenido() == null) {
            return Collections.emptyList();
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(docReadme.getContenido());

            List<String> sections = new ArrayList<>();
            root.fieldNames().forEachRemaining(sections::add);
            return sections;
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    public Map<String, Object> estructuraSecsReadme(Integer idApi) throws JsonProcessingException {
        Documentacion docReadme = documentacionRepository.findByApi_IdApiAndFormatoAndDescripcion(idApi, Documentacion.FormatoDoc.MARKDOWN, "Descripcion Técnica");

        if (docReadme == null || docReadme.getContenido() == null) {
            return Collections.emptyMap();
        }

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(docReadme.getContenido(), new TypeReference<Map<String, Object>>() {});
    }

    public String extraerCabecerasJsonFlexmark(String markdown) throws JsonProcessingException {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);

        Map<String, Object> estructura = new LinkedHashMap<>();
        String currentSec = null;

        for (Node node = document.getFirstChild(); node != null; node = node.getNext()) {
            if (node instanceof Heading heading) {
                String texto = heading.getText().toString();
                int nivel = heading.getLevel();

                if (nivel == 2) { // ## Sección
                    currentSec = texto;
                    estructura.put(currentSec, "");
                } else if (nivel == 3 && currentSec != null) { // ### Sub-sección
                    Object valor = estructura.get(currentSec);
                    if (valor instanceof String) {
                        valor = new LinkedHashMap<String, Object>();
                        estructura.put(currentSec, valor);
                    }
                    ((Map<String, Object>) valor).put(texto, "");
                }
            }
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(estructura);
    }



}