package com.gravifox.tvb.domain.analysis.sse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SseHub {
    private static final Logger log = LoggerFactory.getLogger(SseHub.class);
    private final SnapshotCache snapshotCache;

    // jobId -> emitters (CopyOnWrite to avoid concurrent modification issues during iteration)
    private final Map<String, CopyOnWriteArrayList<SseEmitter>> sessions = new ConcurrentHashMap<>();

    public SseHub(SnapshotCache snapshotCache) {
        this.snapshotCache = snapshotCache;
    }

    /** Open a new SSE connection for the given jobId. Timeout is unlimited (0L). */
    public SseEmitter open(String jobId) {
        SseEmitter emitter = new SseEmitter(0L);
        sessions.computeIfAbsent(jobId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        if (log.isDebugEnabled()) {
            log.debug("[SSE] open connection jobId={}, activeSessions={}"
                    , jobId, activeCount(jobId));
        }

        emitter.onCompletion(() -> remove(jobId, emitter));
        emitter.onTimeout(() -> remove(jobId, emitter));
        emitter.onError(e -> remove(jobId, emitter));

        // Send snapshot once if exists (best-effort)
        SnapshotRecord snap = snapshotCache.get(jobId);
        if (snap != null) {
            try {
                SseEmitter.SseEventBuilder event = SseEmitter.event()
                        .name(snap.event())
                        .data(snap.data(), MediaType.APPLICATION_JSON);
                emitter.send(event);
                if (log.isDebugEnabled()) {
                    log.debug("[SSE] sent snapshot event={} for jobId={}", snap.event(), jobId);
                }
            } catch (IOException ignored) {
                // If sending snapshot fails, just proceed; emitter will stay open for live events
            }
        }

        return emitter;
    }

    /** Send an event to all active emitters for jobId. Removes closed/broken emitters. */
    public void send(String jobId, String event, Object data) {
        if (jobId == null || event == null) return;

        // Update snapshot unless it's heartbeat
        if (!"heartbeat".equals(event)) {
            snapshotCache.put(jobId, event, data);
        }

        List<SseEmitter> list = sessions.getOrDefault(jobId, new CopyOnWriteArrayList<>());
        List<SseEmitter> toRemove = new ArrayList<>();
        int receivers = list.size();
        for (SseEmitter emitter : list) {
            try {
                SseEmitter.SseEventBuilder builder = SseEmitter.event()
                        .name(event)
                        .data(data, MediaType.APPLICATION_JSON);
                emitter.send(builder);
            } catch (IOException e) {
                toRemove.add(emitter);
            }
        }
        if (!"heartbeat".equals(event) && log.isDebugEnabled()) {
            log.debug("[SSE] broadcast event={} to jobId={} receivers={} (removed={})",
                    event, jobId, receivers, toRemove.size());
        }
        // cleanup failed ones
        for (SseEmitter dead : toRemove) {
            remove(jobId, dead);
        }
    }

    public int activeCount(String jobId) {
        List<SseEmitter> list = sessions.get(jobId);
        return list == null ? 0 : list.size();
    }

    /** Returns a snapshot view of all jobIds currently tracked (may change concurrently). */
    public java.util.Set<String> jobIds() {
        return java.util.Collections.unmodifiableSet(sessions.keySet());
    }

    /** Complete and cleanup all emitters for the given job. */
    public void complete(String jobId) {
        CopyOnWriteArrayList<SseEmitter> list = sessions.get(jobId);
        if (list == null) return;
        for (SseEmitter em : list) {
            try { em.complete(); } catch (Throwable ignored) {}
        }
        sessions.remove(jobId);
        if (log.isDebugEnabled()) {
            log.debug("[SSE] complete jobId={}, removedSessions={}", jobId, (list != null ? list.size() : 0));
        }
    }

    private void remove(String jobId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> list = sessions.get(jobId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) {
                sessions.remove(jobId, list);
            }
        }
        try { emitter.complete(); } catch (Throwable ignored) {}
    }
}
