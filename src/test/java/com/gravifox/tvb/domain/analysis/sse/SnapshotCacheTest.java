package com.gravifox.tvb.domain.analysis.sse;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SnapshotCacheTest {

    @Test
    void put_get_invalidate() {
        SnapshotCache cache = new SnapshotCache(24);
        cache.put("jobA", "progress", new java.util.HashMap<>());
        SnapshotRecord rec = cache.get("jobA");
        assertThat(rec).isNotNull();
        assertThat(rec.event()).isEqualTo("progress");

        cache.invalidate("jobA");
        assertThat(cache.get("jobA")).isNull();
    }

    @Test
    void expires_when_ttl_zero() throws Exception {
        SnapshotCache cache = new SnapshotCache(0);
        cache.put("jobB", "progress", java.util.Map.of("p", 50));
        Thread.sleep(5);
        assertThat(cache.get("jobB")).isNull();
    }
}

