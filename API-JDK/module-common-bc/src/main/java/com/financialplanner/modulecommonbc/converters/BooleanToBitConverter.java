package com.financialplanner.modulecommonbc.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * A converter class that implements the AttributeConverter interface
 * to handle the mapping between Boolean values in the entity and
 * their corresponding String representations in the database.
 * This converter maps:
 * - A `true` Boolean value to the string "1" for database storage.
 * - A `false` or null Boolean value to the string "0" for database storage.
 * - The string "1" from the database back to a `true` Boolean value.
 * - Any other string from the database back to a `false` Boolean value.
 * This class is typically used to facilitate the conversion
 * when persisting or retrieving Boolean values from databases where the
 * Boolean type is not supported natively.
 */
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
