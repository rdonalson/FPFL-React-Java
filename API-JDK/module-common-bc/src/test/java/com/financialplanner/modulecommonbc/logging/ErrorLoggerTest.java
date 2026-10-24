package com.financialplanner.modulecommonbc.logging;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ErrorLoggerTest {

    @Test
    void returnsAFreshCorrelationIdForEachLoggedException() {
        String first = ErrorLogger.logException(new IllegalStateException("boom"));
        String second = ErrorLogger.logException(new RuntimeException("again"));

        assertDoesNotThrow(() -> UUID.fromString(first));
        assertNotEquals(first, second);
    }
}
