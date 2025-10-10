package com.gravifox.tvb.domain.analysis.sse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryTokenStore implements TokenStore {
    private static final Logger log = LoggerFactory.getLogger(InMemoryTokenStore.class);
    private final ConcurrentHashMap<String, TokenRecord> store = new ConcurrentHashMap<>();
    private final SecureRandom rng = new SecureRandom();
    private final Duration ttl;
    private final long graceSec;

    public InMemoryTokenStore(
            @Value("${analyze.token-ttl-hours:24}") long tokenTtlHours,
            @Value("${analyze.token-grace-sec:0}") long tokenGraceSec
    ) {
        this.ttl = Duration.ofHours(tokenTtlHours);
        this.graceSec = Math.max(0, tokenGraceSec);
    }

    @Override
    public String issue(String jobId) {
        Objects.requireNonNull(jobId, "jobId");
        String token = randomToken();
        Instant exp = Instant.now().plus(ttl);
        store.put(jobId, new TokenRecord(token, exp));
        if (log.isDebugEnabled()) {
            log.debug("[TokenStore] issued jobId={}, token={}, exp={}", jobId, mask(token), exp);
        }
        return token;
    }

    @Override
    public boolean validate(String jobId, String token) {
        if (jobId == null || token == null) return false;
        TokenRecord rec = store.get(jobId);
        if (rec == null) {
            if (log.isDebugEnabled()) log.debug("[TokenStore] validate miss: no record jobId={}, provided={}", jobId, mask(token));
            return false;
        }
        if (Instant.now().isAfter(rec.expiresAt())) {
            store.remove(jobId, rec);
            if (log.isDebugEnabled()) log.debug("[TokenStore] validate fail: expired jobId={}, exp={}", jobId, rec.expiresAt());
            return false;
        }
        boolean ok = token.equals(rec.token());
        if (!ok && log.isDebugEnabled()) {
            log.debug("[TokenStore] validate fail: mismatch jobId={}, expected={}, provided={}", jobId, mask(rec.token()), mask(token));
        } else if (ok && log.isDebugEnabled()) {
            log.debug("[TokenStore] validate success jobId={}", jobId);
        }
        return ok;
    }

    @Override
    public void invalidate(String jobId) {
        if (jobId == null) return;
        TokenRecord rec = store.get(jobId);
        if (rec == null) return;
        if (graceSec > 0) {
            Instant exp = Instant.now().plusSeconds(graceSec);
            store.put(jobId, new TokenRecord(rec.token(), exp));
            if (log.isDebugEnabled()) log.debug("[TokenStore] invalidated with grace jobId={} graceSec={} exp={}", jobId, graceSec, exp);
        } else {
            store.remove(jobId);
            if (log.isDebugEnabled()) log.debug("[TokenStore] invalidated jobId={}", jobId);
        }
    }

    /** Optional manual cleanup (will be scheduled in later step). */
    public void cleanupExpired() {
        Instant now = Instant.now();
        store.forEach((k, v) -> {
            if (now.isAfter(v.expiresAt())) store.remove(k, v);
        });
    }

    private String randomToken() {
        byte[] buf = new byte[16]; // 128-bit token
        rng.nextBytes(buf);
        return HexFormat.of().formatHex(buf);
    }

    private String mask(String token) {
        if (token == null) return "null";
        int len = token.length();
        if (len <= 10) return "***";
        return token.substring(0, 6) + "..." + token.substring(len - 4);
    }
}
