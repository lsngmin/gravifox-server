package com.gravifox.domain.analysis.controller;

import com.gravifox.domain.analysis.id.Ulid;
import com.gravifox.domain.analysis.service.ModelCatalogService;
import com.gravifox.domain.analysis.service.AnalyzeUsageService;
import com.gravifox.domain.analysis.sse.SseHub;
import com.gravifox.domain.analysis.sse.TokenStore;
import com.gravifox.security.jwt.principal.UserPrincipal;
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
import org.springframework.security.core.Authentication;
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
    private final AnalyzeUsageService analyzeUsageService;

    public AnalyzeJobController(RabbitTemplate rabbitTemplate,
                                TokenStore tokenStore,
                                SseHub sseHub,
                                @Value("${analyze.exchange:analyze.exchange}") String analyzeExchange,
                                ModelCatalogService modelCatalogService,
                                AnalyzeUsageService analyzeUsageService) {
        this.rabbitTemplate = rabbitTemplate;
        this.tokenStore = tokenStore;
        this.sseHub = sseHub;
        this.analyzeExchange = analyzeExchange;
        this.modelCatalogService = modelCatalogService;
        this.analyzeUsageService = analyzeUsageService;
    }

    public static record AnalyzeCreateRequest(@NotBlank String uploadId, Map<String, Object> params, String modelKey) {}
    public static record AnalyzeAcceptedResponse(String jobId, String sseToken, String modelKey, Integer remainingQuota) {}
    public static record ModelCatalogResponse(String defaultKey, List<ModelCatalogService.ModelSummary> items) {}

    @PostMapping(path = "/api/analyze", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AnalyzeAcceptedResponse> createAnalyzeJob(@Valid @RequestBody AnalyzeCreateRequest req,
                                                                    Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal userPrincipal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized");
        }
        Long userNo = Long.parseLong(userPrincipal.getName());
        if (log.isInfoEnabled()) {
            log.info("[Analyze] create request userNo={} uploadId={} modelKey={} authPrincipal={}",
                    userNo, req.uploadId(), req.modelKey(), authentication.getClass().getSimpleName());
        }

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
        payload.put("userNo", userNo);
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

        AnalyzeUsageService.QuotaSnapshot quotaSnapshot = analyzeUsageService.prepareNewJob(
                userNo,
                jobId,
                req.uploadId(),
                modelInfo.key()
        );

        try {
            rabbitTemplate.convertAndSend(analyzeExchange, "analyze.request", payload, m -> {
                m.getMessageProperties().setContentType(MediaType.APPLICATION_JSON_VALUE);
                m.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                return m;
            });
        } catch (Exception e) {
            log.error("Failed to publish analyze.request for jobId={}, uploadId={}", jobId, req.uploadId(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "analyze_publish_failed");
        }

        if (log.isDebugEnabled()) {
            log.debug("[Analyze] request enqueued jobId={}, quotaRemaining={}", jobId, quotaSnapshot.remaining());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.LOCATION, "/api/analyze/" + jobId);
        return new ResponseEntity<>(new AnalyzeAcceptedResponse(jobId, token, modelInfo.key(), quotaSnapshot.remaining()), headers, HttpStatus.ACCEPTED);
    }

    @ExceptionHandler(Exception.class)
    public void handleUnexpectedException(Exception ex) {
        if (ex instanceof ResponseStatusException rse) {
            throw rse;
        }
        log.error("Unexpected error while creating analyze job", ex);
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "analyze_internal_error", ex);
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
