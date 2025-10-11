package com.example.telitodev.service;

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

    public DocMDService() {
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
    }

    public Map<String, String> mapeoSecsDoc(Integer idApi) throws IOException {
        ClassPathResource resource = new ClassPathResource("/static/docsMD/API" + idApi + ".md");
        if (!resource.exists()) {
            return Collections.emptyMap();
        }

        String md = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (md.trim().isEmpty()) {
            return Collections.emptyMap();
        }

        // Dividir por secciones (## Título)
        Map<String, String> sections = new LinkedHashMap<>();
        String[] parts = md.split("(?m)^## "); // regex: línea que empieza con "## "

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
    public List<String> nombresSecsDoc(Integer idApi) throws IOException {

        Set<String> sections = mapeoSecsDoc(idApi).keySet();
        if (sections.isEmpty()) {
            return  Collections.emptyList();
        }

        return new ArrayList<>(sections);
    }
}