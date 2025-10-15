package com.gravifox.domain.analysisreport.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * Classification label produced by AI analysis.
 */
public enum AnalysisLabel {
    AI("ai"),
    REAL("real"),
    UNKNOWN("unknown");

    private final String code;

    AnalysisLabel(String code) {
        this.code = code;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return code;
    }

    @JsonCreator
    public static AnalysisLabel from(String value) {
        if (value == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(it -> it.code.equalsIgnoreCase(value) || it.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported analysis label: " + value));
    }
}
