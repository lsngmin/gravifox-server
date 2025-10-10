package com.gravifox.domain.analysis.sse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class HeartbeatScheduler {
    private static final Logger log = LoggerFactory.getLogger(HeartbeatScheduler.class);

    private final SseHub sseHub;
    private final SnapshotCache snapshotCache;
    private final TokenStore tokenStore;

    private final long heartbeatSec;

    public HeartbeatScheduler(SseHub sseHub,
                               SnapshotCache snapshotCache,
                               TokenStore tokenStore,
                               @Value("${analyze.heartbeat-sec:15}") long heartbeatSec) {
        this.sseHub = sseHub;
        this.snapshotCache = snapshotCache;
        this.tokenStore = tokenStore;
        this.heartbeatSec = heartbeatSec;
    }

    /** Broadcast heartbeat to all active job SSE sessions. */
    @Scheduled(fixedDelayString = "${analyze.heartbeat-sec:15}000")
    public void broadcastHeartbeat() {
        long ts = Instant.now().getEpochSecond();
        Map<String, Object> hb = new HashMap<>();
        hb.put("ts", ts);
        int total = 0;
        for (String jobId : sseHub.jobIds()) {
            if (sseHub.activeCount(jobId) > 0) {
                sseHub.send(jobId, "heartbeat", hb);
                total++;
            }
        }
        if (total > 0 && heartbeatSec >= 10) {
            log.debug("sent heartbeat to {} job(s)", total);
        }
    }

    /** Periodic cleanup for expired tokens and snapshots. */
    @Scheduled(fixedDelayString = "60000")
    public void cleanup() {
        snapshotCache.cleanupExpired();
        // Best-effort: cleanup only if implementation offers it
        if (tokenStore instanceof InMemoryTokenStore mem) {
            mem.cleanupExpired();
        }
    }
}

