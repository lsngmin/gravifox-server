package com.gravifox.tvb.domain.analysis.sse;

import java.time.Instant;

public record TokenRecord(String token, Instant expiresAt) {}

