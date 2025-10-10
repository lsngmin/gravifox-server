package com.gravifox.domain.analysis.controller;

import com.gravifox.domain.analysis.id.Ulid;
import com.gravifox.domain.analysis.service.ModelCatalogService;
import com.gravifox.domain.analysis.sse.SseHub;
import com.gravifox.domain.analysis.sse.TokenStore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Validated
public class AnalyzeJobController {
    private static final Logger log = LoggerFactory.getLogger(AnalyzeJobController.class);

    private final RabbitTemplate rabbitTemplate;
    private final TokenStore tokenStore;
    private final SseHub sseHub;
    private final String analyzeExchange;
    private final ModelCatalogService modelCatalogService;

    public AnalyzeJobController(RabbitTemplate rabbitTemplate,
                                TokenStore tokenStore,
                                SseHub sseHub,
                                @Value("${analyze.exchange:analyze.exchange}") String analyzeExchange,
                                ModelCatalogService modelCatalogService) {
        this.rabbitTemplate = rabbitTemplate;
        this.tokenStore = tokenStore;
        this.sseHub = sseHub;
        this.analyzeExchange = analyzeExchange;
        this.modelCatalogService = modelCatalogService;
    }

    public static record AnalyzeCreateRequest(@NotBlank String uploadId, Map<String, Object> params, String modelKey) {}
    public static record AnalyzeAcceptedResponse(String jobId, String sseToken, String modelKey) {}
    public static record ModelCatalogResponse(String defaultKey, List<ModelCatalogService.ModelSummary> items) {}

    @PostMapping(path = "/api/analyze", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AnalyzeAcceptedResponse> createAnalyzeJob(@Valid @RequestBody AnalyzeCreateRequest req) {
        String jobId = Ulid.generate();
        String token = tokenStore.issue(jobId);
        if (log.isDebugEnabled()) {
            log.debug("[Analyze] create job jobId={}, uploadId={}", jobId, req.uploadId());
        }

        ModelCatalogService.ModelInfo modelInfo;
        try {
            modelInfo = modelCatalogService.resolve(req.modelKey());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unknown_model");
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
        }

        // Build MQ payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("jobId", jobId);
        payload.put("uploadId", req.uploadId());
        payload.put("model", Map.of(
                "key", modelInfo.key(),
                "name", modelInfo.name(),
                "version", modelInfo.version(),
                "type", modelInfo.type()
        ));
        Map<String, Object> mergedParams = new HashMap<>();
        if (req.params() != null) {
            mergedParams.putAll(req.params());
        }
        mergedParams.put("modelKey", modelInfo.key());
        payload.put("params", mergedParams);

        try {
            rabbitTemplate.convertAndSend(analyzeExchange, "analyze.request", payload, m -> {
                m.getMessageProperties().setContentType(MediaType.APPLICATION_JSON_VALUE);
                m.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                return m;
            });
        } catch (Exception e) {
            // Spec: MQ 장애 시에도 202 응답. 로깅만 수행.
            log.warn("Failed to publish analyze.request for jobId={}", jobId, e);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.LOCATION, "/api/analyze/" + jobId);
        return new ResponseEntity<>(new AnalyzeAcceptedResponse(jobId, token, modelInfo.key()), headers, HttpStatus.ACCEPTED);
    }

    @GetMapping(path = "/api/analyze/models", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ModelCatalogResponse> listModels() {
        ModelCatalogService.CatalogData catalog = modelCatalogService.fetchCatalog();
        List<ModelCatalogService.ModelSummary> summaries = modelCatalogService.listSummaries();
        return ResponseEntity.ok(new ModelCatalogResponse(catalog.defaultKey(), summaries));
    }

    @GetMapping(path = "/api/analyze/{jobId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAnalyzeEvents(@PathVariable("jobId") String jobId,
                                          @RequestParam(name = "token", required = false) String token,
                                          HttpServletResponse response) {
        if (log.isDebugEnabled()) {
            log.debug("[Analyze] SSE connect attempt jobId={}, hasToken={}", jobId, token != null);
        }
        if (token == null || !tokenStore.validate(jobId, token)) {
            if (log.isWarnEnabled()) log.warn("[Analyze] SSE token invalid or expired jobId={}", jobId);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "sse_token_invalid");
        }

        // Set SSE-friendly headers
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.setHeader("X-Accel-Buffering", "no");
        return sseHub.open(jobId);
    }
}
