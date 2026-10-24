package com.financialplanner.modulecommonbc.converters;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BooleanToBitConverterTest {

    private final BooleanToBitConverter converter = new BooleanToBitConverter();

    @Test
    void writesOneForTrueAndZeroOtherwise() {
        assertEquals("1", converter.convertToDatabaseColumn(true));
        assertEquals("0", converter.convertToDatabaseColumn(false));
        assertEquals("0", converter.convertToDatabaseColumn(null));
    }

    @Test
    void readsOnlyOneAsTrue() {
        assertTrue(converter.convertToEntityAttribute("1"));
        assertFalse(converter.convertToEntityAttribute("0"));
        assertFalse(converter.convertToEntityAttribute(null));
        assertFalse(converter.convertToEntityAttribute("true"));
    }
}
