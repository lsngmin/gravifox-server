package com.gravifox.tvb.domain.analysis.sse;

import com.gravifox.tvb.domain.analysis.id.Ulid;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryTokenStoreTest {

    @Test
    void issue_validate_invalidate_flow() {
        InMemoryTokenStore store = new InMemoryTokenStore(24); // 24h TTL
        String jobId = Ulid.generate();
        String token = store.issue(jobId);

        assertThat(token).isNotBlank();
        assertThat(store.validate(jobId, token)).isTrue();
        assertThat(store.validate(jobId, token + "x")).isFalse();
        assertThat(store.validate("wrongJob", token)).isFalse();

        store.invalidate(jobId);
        assertThat(store.validate(jobId, token)).isFalse();
    }

    @Test
    void expires_when_ttl_is_zero_hours() throws Exception {
        InMemoryTokenStore store = new InMemoryTokenStore(0); // immediate expiry
        String jobId = Ulid.generate();
        String token = store.issue(jobId);
        // ensure clock moves past expiration boundary
        Thread.sleep(5);
        assertThat(store.validate(jobId, token)).isFalse();
    }
}

