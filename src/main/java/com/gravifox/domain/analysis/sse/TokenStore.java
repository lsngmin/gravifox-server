package com.gravifox.domain.analysis.sse;

public interface TokenStore {
    /** Issue a new token for the job and store it with TTL. Returns the token string. */
    String issue(String jobId);

    /** Validate token for the job. Returns true only if matches and not expired. */
    boolean validate(String jobId, String token);

    /** Invalidate token immediately (on result/failed). */
    void invalidate(String jobId);
}

