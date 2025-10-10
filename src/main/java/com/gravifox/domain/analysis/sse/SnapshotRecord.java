package com.gravifox.domain.analysis.sse;

import java.time.Instant;

public record SnapshotRecord(String event, Object data, Instant expiresAt) {}

