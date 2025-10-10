package com.gravifox.tvb.domain.analysis.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class ModelCatalogService {
    private static final Logger log = LoggerFactory.getLogger(ModelCatalogService.class);

    private final Path catalogPath;
    private final ObjectMapper objectMapper;
    private final AtomicReference<Cache> cache = new AtomicReference<>();

    public ModelCatalogService(@Value("${analyze.models.catalog-path}") String catalogPath,
                               ObjectMapper objectMapper) {
        this.catalogPath = Path.of(catalogPath).toAbsolutePath().normalize();
        this.objectMapper = objectMapper;
        log.info("Model catalog path resolved to {}", this.catalogPath);
    }

    public CatalogData fetchCatalog() {
        Cache current = cache.get();
        FileTime mtime = lastModified();
        if (current != null && Objects.equals(current.mtime(), mtime)) {
            return current.data();
        }
        synchronized (this) {
            current = cache.get();
            if (current != null && Objects.equals(current.mtime(), mtime)) {
                return current.data();
            }
            CatalogData loaded = loadCatalog();
            cache.set(new Cache(mtime, loaded));
            return loaded;
        }
    }

    public ModelInfo resolve(String requestedKey) {
        CatalogData catalog = fetchCatalog();
        if (requestedKey != null && !requestedKey.isBlank()) {
            String key = requestedKey.trim();
            return catalog.items().stream()
                    .filter(m -> m.key().equals(key))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("unknown_model"));
        }
        String defaultKey = catalog.defaultKey();
        if (defaultKey != null && !defaultKey.isBlank()) {
            return catalog.items().stream()
                    .filter(m -> m.key().equals(defaultKey))
                    .findFirst()
                    .orElseGet(() -> {
                        throw new IllegalStateException("default model key not found in catalog");
                    });
        }
        return catalog.items().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("model catalog is empty"));
    }

    public List<ModelSummary> listSummaries() {
        CatalogData catalog = fetchCatalog();
        List<ModelSummary> summaries = new ArrayList<>();
        for (ModelInfo info : catalog.items()) {
            summaries.add(new ModelSummary(
                    info.key(),
                    info.name(),
                    info.version(),
                    info.description(),
                    info.type(),
                    info.input(),
                    info.threshold(),
                    info.labels()
            ));
        }
        return summaries;
    }

    private CatalogData loadCatalog() {
        if (!Files.exists(catalogPath)) {
            throw new IllegalStateException("model catalog not found: " + catalogPath);
        }
        try (var reader = Files.newBufferedReader(catalogPath)) {
            JsonNode root = objectMapper.readTree(reader);
            String defaultKey = textNode(root.path("defaultKey"));
            List<ModelInfo> models = new ArrayList<>();
            for (JsonNode item : root.path("items")) {
                String key = textNode(item.path("key"));
                if (key == null || key.isBlank()) {
                    continue;
                }
                ModelInfo info = new ModelInfo(
                        key.trim(),
                        defaultText(item.path("name"), key),
                        textNode(item.path("version")),
                        textNode(item.path("description")),
                        defaultText(item.path("type"), "torch_image"),
                        defaultText(item.path("input"), "image").toLowerCase(Locale.ROOT),
                        item.path("threshold").asDouble(0.5d),
                        parseLabels(item.path("labels")),
                        textNode(item.path("path"))
                );
                models.add(info);
            }
            if (models.isEmpty()) {
                throw new IllegalStateException("model catalog contains no items: " + catalogPath);
            }
            return new CatalogData(defaultKey, Collections.unmodifiableList(models));
        } catch (IOException e) {
            throw new IllegalStateException("failed to read model catalog: " + catalogPath, e);
        }
    }

    private FileTime lastModified() {
        try {
            return Files.getLastModifiedTime(catalogPath);
        } catch (IOException e) {
            throw new IllegalStateException("failed to read catalog timestamp: " + catalogPath, e);
        }
    }

    private static List<String> parseLabels(JsonNode node) {
        if (node == null || node.isMissingNode() || !node.isArray()) {
            return List.of();
        }
        List<String> labels = new ArrayList<>();
        node.forEach(n -> {
            String v = textNode(n);
            if (v != null && !v.isBlank()) {
                labels.add(v.trim());
            }
        });
        return List.copyOf(labels);
    }

    private static String textNode(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        String text = node.asText(null);
        return text != null && !text.isBlank() ? text : null;
    }

    private static String defaultText(JsonNode node, String fallback) {
        return Optional.ofNullable(textNode(node)).orElse(fallback);
    }

    private record Cache(FileTime mtime, CatalogData data) {}

    public record CatalogData(String defaultKey, List<ModelInfo> items) {}

    public record ModelInfo(
            String key,
            String name,
            String version,
            String description,
            String type,
            String input,
            double threshold,
            List<String> labels,
            String path
    ) {}

    public record ModelSummary(
            String key,
            String name,
            String version,
            String description,
            String type,
            String input,
            double threshold,
            List<String> labels
    ) {}
}
