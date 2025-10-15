package com.gravifox.domain.analysisreport.domain.converter;

import com.gravifox.domain.analysisreport.domain.AnalysisLabel;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Stores analysis labels using lowercase codes.
 */
@Converter(autoApply = true)
public class AnalysisLabelConverter implements AttributeConverter<AnalysisLabel, String> {

    @Override
    public String convertToDatabaseColumn(AnalysisLabel attribute) {
        return attribute != null ? attribute.getCode() : null;
    }

    @Override
    public AnalysisLabel convertToEntityAttribute(String dbData) {
        return dbData != null ? AnalysisLabel.from(dbData) : null;
    }
}
