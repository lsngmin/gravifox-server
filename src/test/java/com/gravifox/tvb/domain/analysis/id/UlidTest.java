package com.gravifox.tvb.domain.analysis.id;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UlidTest {

    @Test
    void generates_26_chars_with_crockford_charset() {
        String id = Ulid.generate();
        assertThat(id).hasSize(26);
        String allowed = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";
        for (char c : id.toCharArray()) {
            assertThat(allowed.indexOf(c)).isGreaterThanOrEqualTo(0);
        }
    }

    @Test
    void mostly_time_ordered() throws Exception {
        String a = Ulid.generate();
        Thread.sleep(2);
        String b = Ulid.generate();
        assertThat(a.compareTo(b)).isLessThan(0);
    }

    @Test
    void high_uniqueness_over_many_generations() {
        java.util.Set<String> set = new java.util.HashSet<>();
        for (int i = 0; i < 1000; i++) {
            set.add(Ulid.generate());
        }
        assertThat(set).hasSize(1000);
    }
}

