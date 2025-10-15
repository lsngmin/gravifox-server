package com.gravifox.domain.analysisreport.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * Media type for analysis reports. Stored as lowercase code in the DB.
 */
public enum AnalysisMediaType {
    IMAGE("image"),
    VIDEO("video");

    private final String code;

    AnalysisMediaType(String code) {
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
    public static AnalysisMediaType from(String value) {
        if (value == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(it -> it.code.equalsIgnoreCase(value) || it.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported media type: " + value));
    }
}
