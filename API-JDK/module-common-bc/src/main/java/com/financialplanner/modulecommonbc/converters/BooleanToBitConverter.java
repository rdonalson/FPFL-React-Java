package com.financialplanner.modulecommonbc.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class BooleanToBitConverter implements AttributeConverter<Boolean, String> {

    @Override
    public String convertToDatabaseColumn(Boolean value) {
        return (value != null && value) ? "1" : "0";
    }

    @Override
    public Boolean convertToEntityAttribute(String dbValue) {
        return "1".equals(dbValue);
    }
}
