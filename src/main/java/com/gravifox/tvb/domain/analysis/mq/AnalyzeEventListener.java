package com.gravifox.tvb.domain.analysis.mq;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gravifox.tvb.domain.analysis.sse.SseHub;
import com.gravifox.tvb.domain.analysis.sse.TokenStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class AnalyzeEventListener {
    private static final Logger log = LoggerFactory.getLogger(AnalyzeEventListener.class);

    private final ObjectMapper objectMapper;
    private final SseHub sseHub;
    private final TokenStore tokenStore;

    public AnalyzeEventListener(ObjectMapper objectMapper, SseHub sseHub, TokenStore tokenStore) {
        this.objectMapper = objectMapper;
        this.sseHub = sseHub;
        this.tokenStore = tokenStore;
    }

    @RabbitListener(queues = "#{analyzeBridgeQueue.name}")
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
