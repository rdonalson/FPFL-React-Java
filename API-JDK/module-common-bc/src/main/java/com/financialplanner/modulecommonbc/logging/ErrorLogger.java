package com.financialplanner.modulecommonbc.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for logging exceptions and generating unique correlation IDs for traceability.
 * This class provides a static method to log exception details using the SLF4J logging framework
 * and associates each logged exception with a generated correlation ID. The correlation ID is
 * intended to aid in tracing and debugging application issues by linking logged errors to their
 * respective context.
 * Key features:
 * - Generates a unique correlation ID for each exception logged.
 * - Logs the exception message and stack trace at the error level.
 * - Supports integration with application-level exception handlers to enhance observability.
 * Thread Safety:
 * - This class is thread-safe, as it does not maintain any mutable state and the SLF4J logger
 * implementation is designed to handle concurrent logging calls.
 */
public class ErrorLogger {

    private static final Logger LOG = LoggerFactory.getLogger(ErrorLogger.class);

    public static String logException(Exception ex) {
        String correlationId = java.util.UUID.randomUUID()
                                             .toString();
        LOG.error("Correlation ID: {} - Exception: {}", correlationId, ex.getMessage(), ex);
        return correlationId;
    }
}
