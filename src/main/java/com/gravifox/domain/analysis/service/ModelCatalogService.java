package com.gravifox.domain.analysis.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class ModelCatalogService {
    private static final Logger log = LoggerFactory.getLogger(ModelCatalogService.class);

    private final String catalogPath;
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final AtomicReference<Cache> cache = new AtomicReference<>();

    public ModelCatalogService(
            @Value("${analyze.models.catalog-path:catalog.json}") String catalogPath,
            ObjectMapper objectMapper,
            ResourceLoader resourceLoader
    ) {
        this.catalogPath = catalogPath;
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
        log.info("Model catalog path configured as {}", this.catalogPath);
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
                    .orElseThrow(() -> new IllegalStateException("default model key not found in catalog"));
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

    // --------- Internal helpers ---------
    private CatalogData loadCatalog() {
        Resource resource = resolveResource();
        if (!resource.exists()) {
            throw new IllegalStateException("model catalog not found: " + resource.getDescription());
        }

        try (InputStream in = resource.getInputStream()) {
            JsonNode root = objectMapper.readTree(in);
            String defaultKey = textNode(root.path("defaultKey"));
            List<ModelInfo> models = new ArrayList<>();

            for (JsonNode item : root.path("items")) {
                String key = textNode(item.path("key"));
                if (key == null || key.isBlank()) continue;
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
                throw new IllegalStateException("model catalog contains no items: " + resource.getDescription());
            }

            log.info("Loaded {} models from {}", models.size(), resource.getDescription());
            return new CatalogData(defaultKey, Collections.unmodifiableList(models));
        } catch (IOException e) {
            throw new IllegalStateException("failed to read model catalog: " + resource.getDescription(), e);
        }
    }

    private Resource resolveResource() {
        // 1️⃣ 직접 지정된 경로
        Resource res = resourceLoader.getResource(catalogPath);
        if (res.exists()) return res;

        // 2️⃣ classpath:/static fallback
        Resource staticRes = resourceLoader.getResource("classpath:/static/" + catalogPath);
        if (staticRes.exists()) return staticRes;

        // 3️⃣ classpath:/ fallback
        Resource rootRes = resourceLoader.getResource("classpath:/" + catalogPath);
        if (rootRes.exists()) return rootRes;

        log.warn("Model catalog not found via {}, trying fallback paths", catalogPath);
        return res; // return original (will cause not found error later)
    }

    private FileTime lastModified() {
        try {
            Resource res = resolveResource();
            if (res.isFile()) {
                return Files.getLastModifiedTime(res.getFile().toPath());
            }
        } catch (Exception ignored) {}
        return FileTime.fromMillis(0);
    }

    // --------- Utility methods ---------
    private static List<String> parseLabels(JsonNode node) {
        if (node == null || node.isMissingNode() || !node.isArray()) {
            return List.of();
        }
        List<String> labels = new ArrayList<>();
        node.forEach(n -> {
            String v = textNode(n);
            if (v != null && !v.isBlank()) labels.add(v.trim());
        });
        return List.copyOf(labels);
    }

    private static String textNode(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        String text = node.asText(null);
        return (text != null && !text.isBlank()) ? text : null;
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
