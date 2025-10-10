package com.gravifox.domain.analysis.sse;

import java.time.Instant;

public record TokenRecord(String token, Instant expiresAt) {}

