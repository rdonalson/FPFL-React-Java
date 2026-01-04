package com.financialplanner.moduleapi.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorLogger {

    private static final Logger log = LoggerFactory.getLogger(ErrorLogger.class);

    public static String logException(Exception ex) {
        String correlationId = java.util.UUID.randomUUID()
                                             .toString();
        log.error("Correlation ID: {} - Exception: {}", correlationId, ex.getMessage(), ex);
        return correlationId;
    }
}
