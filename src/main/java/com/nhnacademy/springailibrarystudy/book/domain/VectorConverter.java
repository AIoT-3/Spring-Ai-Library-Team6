package com.nhnacademy.springailibrarystudy.book.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;

// FIXME: (임시) 원본 그대로 가져옴
@Converter
public class VectorConverter implements AttributeConverter<float[], String> {

    @Override
    public String convertToDatabaseColumn(float[] attribute) {
        if (attribute == null || attribute.length == 0) {
            return null;
        }
        return Arrays.toString(attribute);
    }

    @Override
    public float[] convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        String cleanData = dbData.substring(1, dbData.length() - 1);
        if (cleanData.isBlank()) {
            return new float[0];
        }

        String[] parts = cleanData.split(",");
        float[] vector = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            vector[i] = Float.parseFloat(parts[i].trim());
        }
        return vector;
    }
}
