package com.gravifox.domain.analysis.sse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SnapshotCache {
    private final ConcurrentHashMap<String, SnapshotRecord> store = new ConcurrentHashMap<>();
    private final Duration ttl;

    public SnapshotCache(@Value("${analyze.snapshot-ttl-hours:24}") long ttlHours) {
        this.ttl = Duration.ofHours(ttlHours);
    }

    public void put(String jobId, String event, Object data) {
        if (jobId == null || event == null) return;
        Instant exp = Instant.now().plus(ttl);
        store.put(jobId, new SnapshotRecord(event, data, exp));
    }

    public SnapshotRecord get(String jobId) {
        if (jobId == null) return null;
        SnapshotRecord rec = store.get(jobId);
        if (rec == null) return null;
        if (Instant.now().isAfter(rec.expiresAt())) {
            store.remove(jobId, rec);
            return null;
        }
        return rec;
    }

    public void invalidate(String jobId) {
        if (jobId != null) store.remove(jobId);
    }

    public void cleanupExpired() {
        Instant now = Instant.now();
        store.forEach((k, v) -> {
            if (now.isAfter(v.expiresAt())) store.remove(k, v);
        });
    }
}

