package com.gravifox.domain.analysisreport.domain.converter;

import com.gravifox.domain.analysisreport.domain.AnalysisMediaType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Persists media types as lowercase codes for forward compatibility with new enum constants.
 */
@Converter(autoApply = true)
public class AnalysisMediaTypeConverter implements AttributeConverter<AnalysisMediaType, String> {

    @Override
    public String convertToDatabaseColumn(AnalysisMediaType attribute) {
        return attribute != null ? attribute.getCode() : null;
    }

    @Override
    public AnalysisMediaType convertToEntityAttribute(String dbData) {
        return dbData != null ? AnalysisMediaType.from(dbData) : null;
    }
}
