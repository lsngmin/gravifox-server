package com.gravifox.domain.analysis.mq;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gravifox.domain.analysis.domain.AnalyzeJob;
import com.gravifox.domain.analysis.repository.AnalyzeJobRepository;
import com.gravifox.domain.analysis.service.AnalyzeUsageService;
import com.gravifox.domain.analysisreport.domain.AnalysisLabel;
import com.gravifox.domain.analysisreport.domain.AnalysisMediaType;
import com.gravifox.domain.analysisreport.dto.AnalysisReportUpsertRequest;
import com.gravifox.domain.analysisreport.service.AnalysisReportService;
import com.gravifox.domain.analysis.sse.SseHub;
import com.gravifox.domain.analysis.sse.TokenStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class AnalyzeEventListener {
    private static final Logger log = LoggerFactory.getLogger(AnalyzeEventListener.class);

    private final ObjectMapper objectMapper;
    private final SseHub sseHub;
    private final TokenStore tokenStore;
    private final AnalyzeUsageService analyzeUsageService;
    private final AnalyzeJobRepository analyzeJobRepository;
    private final AnalysisReportService analysisReportService;

    public AnalyzeEventListener(ObjectMapper objectMapper, SseHub sseHub, TokenStore tokenStore,
                                AnalyzeUsageService analyzeUsageService,
                                AnalyzeJobRepository analyzeJobRepository,
                                AnalysisReportService analysisReportService) {
        this.objectMapper = objectMapper;
        this.sseHub = sseHub;
        this.tokenStore = tokenStore;
        this.analyzeUsageService = analyzeUsageService;
        this.analyzeJobRepository = analyzeJobRepository;
        this.analysisReportService = analysisReportService;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "#{analyzeBridgeQueue.name}", durable = "true"),
            exchange = @Exchange(value = "#{analyzeExchange.name}", type = "topic"),
            key = {
                    "analyze.progress.*",
                    "analyze.result.*",
                    "analyze.failed.*"
            }
    ))
    public void handle(Message message) {
        String routing = message.getMessageProperties().getReceivedRoutingKey();
        String event = extractEvent(routing);
        String jobId = extractJobId(routing);

        if (log.isDebugEnabled()) {
            log.debug("[MQ] received routingKey={}, resolved event={}, jobId={}", routing, event, jobId);
        }

        if (event == null || jobId == null) {
            log.warn("Ignore message with routingKey={} (event/jobId not resolved)", routing);
            return;
        }

        Map<String, Object> payload = parseBodyAsJson(message);
        // Ensure jobId present in payload
        payload.putIfAbsent("jobId", jobId);

        try {
            sseHub.send(jobId, event, payload);
            if ("result".equals(event)) {
                try {
                    analyzeUsageService.markJobCompleted(jobId);
                } catch (Exception e) {
                    log.warn("Failed to mark quota completion for jobId={}", jobId, e);
                }
                persistAnalysisResult(jobId, payload);
            } else if ("failed".equals(event)) {
                try {
                    analyzeUsageService.markJobFailed(jobId);
                } catch (Exception e) {
                    log.warn("Failed to mark quota failure for jobId={}", jobId, e);
                }
            }
            if ("result".equals(event) || "failed".equals(event)) {
                if (log.isDebugEnabled()) {
                    log.debug("[MQ] event={} triggers token invalidate for jobId={}", event, jobId);
                }
                tokenStore.invalidate(jobId);
                // Complete SSE sessions for this job to release resources
                try {
                    sseHub.complete(jobId);
                } catch (Exception ignore) {}
            }
        } catch (Exception e) {
            log.error("Failed to fanout SSE for jobId={}, event={}", jobId, event, e);
            // Let AUTO ack proceed; if at-least-once semantics needed, switch to manual ack later
        }
    }

    @SuppressWarnings("unchecked")
    private void persistAnalysisResult(String jobId, Map<String, Object> payload) {
        Object resultObj = payload.get("result");
        if (resultObj == null) {
            log.debug("[MQ] result payload missing for jobId={}, skip persistence", jobId);
            return;
        }
        Map<String, Object> resultMap;
        try {
            if (resultObj instanceof Map<?, ?> rawMap) {
                resultMap = (Map<String, Object>) rawMap;
            } else {
                resultMap = objectMapper.convertValue(resultObj, new TypeReference<>() {});
            }
        } catch (IllegalArgumentException e) {
            log.warn("[MQ] Unable to convert result payload to Map for jobId={}", jobId, e);
            return;
        }

        Optional<AnalyzeJob> jobOptional = analyzeJobRepository.findById(jobId);
        if (jobOptional.isEmpty()) {
            log.warn("[MQ] AnalyzeJob not found for jobId={}, skip result persistence", jobId);
            return;
        }
        AnalyzeJob job = jobOptional.get();

        try {
            AnalysisReportUpsertRequest request = buildUpsertRequest(job, resultMap);
            analysisReportService.upsertReport(job.getUserNo(), request);
        } catch (Exception e) {
            log.error("[MQ] Failed to persist analysis report for jobId={}, uploadId={}", jobId, job.getUploadId(), e);
        }
    }

    private AnalysisReportUpsertRequest buildUpsertRequest(AnalyzeJob job, Map<String, Object> result) {
        AnalysisLabel label = resolveLabel(result);
        BigDecimal score = resolveScore(result, label);
        AnalysisMediaType mediaType = resolveMediaType(result);
        String modelVersion = resolveModelVersion(result, job);
        String heatmapJson = toJson(result.get("heatmap"), "{}");
        String metaJson = toJson(result, null);
        Integer inferenceTimeMs = extractInferenceTime(result);
        String inputResolution = extractInputResolution(result);

        return new AnalysisReportUpsertRequest(
                job.getUploadId(),
                mediaType,
                label,
                score,
                modelVersion,
                heatmapJson,
                metaJson,
                inferenceTimeMs,
                inputResolution
        );
    }

    private AnalysisLabel resolveLabel(Map<String, Object> result) {
        String raw = asString(result.get("label"));
        if (raw == null) {
            raw = asString(result.get("decision"));
        }
        if (raw == null) {
            return AnalysisLabel.UNKNOWN;
        }
        return switch (raw.toLowerCase()) {
            case "ai", "fake" -> AnalysisLabel.AI;
            case "real" -> AnalysisLabel.REAL;
            case "retry", "unknown" -> AnalysisLabel.UNKNOWN;
            default -> AnalysisLabel.UNKNOWN;
        };
    }

    private BigDecimal resolveScore(Map<String, Object> result, AnalysisLabel label) {
        Number candidate = asNumber(result.get("confidence"));
        if (candidate == null && label == AnalysisLabel.AI) {
            candidate = asNumber(result.get("prob_fake"));
        }
        if (candidate == null && label == AnalysisLabel.REAL) {
            candidate = asNumber(result.get("prob_real"));
        }
        if (candidate == null) {
            candidate = asNumber(result.get("pAi"));
        }
        if (candidate == null) {
            candidate = asNumber(result.get("pReal"));
        }
        double value = candidate != null ? candidate.doubleValue() : 0.0;
        double clamped = Math.max(0.0, Math.min(1.0, value));
        return BigDecimal.valueOf(clamped).setScale(4, RoundingMode.HALF_UP);
    }

    private AnalysisMediaType resolveMediaType(Map<String, Object> result) {
        Object inferenceObj = result.get("inference");
        if (inferenceObj instanceof Map<?, ?> inference) {
            String mode = asString(inference.get("mode"));
            if ("video".equalsIgnoreCase(mode)) {
                return AnalysisMediaType.VIDEO;
            }
        }
        String mediaType = asString(result.get("mediaType"));
        if ("video".equalsIgnoreCase(mediaType)) {
            return AnalysisMediaType.VIDEO;
        }
        return AnalysisMediaType.IMAGE;
    }

    private String resolveModelVersion(Map<String, Object> result, AnalyzeJob job) {
        String fromResult = asString(result.get("modelVersion"));
        if (fromResult != null && !fromResult.isBlank()) {
            return fromResult;
        }
        String inferred = job.getModelKey();
        return (inferred == null || inferred.isBlank()) ? "unknown" : inferred;
    }

    private Integer extractInferenceTime(Map<String, Object> result) {
        Object inferenceObj = result.get("inference");
        if (inferenceObj instanceof Map<?, ?> inference) {
            Number latency = asNumber(inference.get("latencyMs"));
            if (latency != null) {
                return (int) Math.round(latency.doubleValue());
            }
        }
        Number latency = asNumber(result.get("latencyMs"));
        if (latency != null) {
            return (int) Math.round(latency.doubleValue());
        }
        return null;
    }

    private String extractInputResolution(Map<String, Object> result) {
        Object inferenceObj = result.get("inference");
        if (inferenceObj instanceof Map<?, ?> inference) {
            String resolution = asString(inference.get("inputResolution"));
            if (resolution != null && !resolution.isBlank()) {
                return resolution;
            }
            String aggregate = asString(inference.get("aggregate"));
            if (aggregate != null && aggregate.contains("x")) {
                return aggregate;
            }
        }
        return null;
    }

    private String toJson(Object value, String defaultJson) {
        if (value == null) {
            return defaultJson;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            log.warn("[MQ] Failed to serialize JSON value, using default fallback", e);
            return defaultJson;
        }
    }

    private String asString(Object value) {
        if (value instanceof String s) {
            return s;
        }
        return value != null ? String.valueOf(value) : null;
    }

    private Number asNumber(Object value) {
        if (value instanceof Number number) {
            return number;
        }
        if (value instanceof String s) {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException ignore) {
            }
        }
        return null;
    }

    private Map<String, Object> parseBodyAsJson(Message message) {
        byte[] body = message.getBody();
        if (body == null || body.length == 0) return new HashMap<>();
        try {
            return objectMapper.readValue(body, new TypeReference<>() {});
        } catch (Exception e) {
            // Fallback: treat as plain text
            String text = new String(body, StandardCharsets.UTF_8);
            Map<String, Object> map = new HashMap<>();
            map.put("text", text);
            return map;
        }
    }

    private String extractEvent(String routingKey) {
        if (routingKey == null) return null;
        if (routingKey.startsWith("analyze.progress.")) return "progress";
        if (routingKey.startsWith("analyze.result.")) return "result";
        if (routingKey.startsWith("analyze.failed.")) return "failed";
        return null;
    }

    private String extractJobId(String routingKey) {
        if (routingKey == null) return null;
        int lastDot = routingKey.lastIndexOf('.');
        if (lastDot < 0 || lastDot + 1 >= routingKey.length()) return null;
        return routingKey.substring(lastDot + 1);
    }
}
