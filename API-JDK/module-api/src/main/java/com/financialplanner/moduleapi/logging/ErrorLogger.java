package com.financialplanner.moduleapi.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class responsible for logging errors and exception in a structured and traceable manner.
 * ---
 * The ErrorLogger class provides a centralized mechanism to log exception along with
 * a unique correlation ID, enabling better tracking and debugging of issues across different
 * parts of the application. The correlation ID serves as a unique identifier that links
 * error log entries with corresponding client-facing error responses.
 * ---
 * This class is designed to be static and thread-safe, allowing for easy integration into
 * various parts of the application without requiring an instance.
 */
public class ErrorLogger {

    /**
     * Logger instance used for logging error messages and exception within the ErrorLogger class.
     * It provides a mechanism to log messages with different levels of severity and supports
     * structured logging for better traceability of logs.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ErrorLogger.class);

    /**
     * Logs the provided exception with a uniquely generated correlation ID and returns the correlation ID.
     * ---
     * The method generates a UUID to serve as the correlation ID, logs the exception along with the
     * correlation ID for tracking purposes, and returns the generated correlation ID. This can be used
     * to link error log entries with client-facing error responses in diagnostic contexts.
     * ---
     *
     * @param ex the exception to be logged; must not be null
     * @return the generated correlation ID associated with the logged exception
     */
    public static String logException(Exception ex) {
        String correlationId = java.util.UUID.randomUUID()
                                             .toString();
        LOG.error("Correlation ID: {} - Exception: {}", correlationId, ex.getMessage(), ex);
        return correlationId;
    }
}
