package com.financialplanner.moduleapi.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class that provides centralized error logging functionality.
 *
 * The ErrorLogger is designed to log exceptions and generate a unique correlation ID
 * for each logged exception, enabling better traceability and debugging of issues
 * in distributed systems or applications with complex workflows.
 *
 * Core Features:
 * - Logs an exception with the associated error message and stack trace.
 * - Generates and returns a unique correlation ID for identifying the specific error occurrence.
 * - Utilizes SLF4J for standardized logging.
 *
 * Note:
 * This class should be leveraged in scenarios where capturing and logging exceptions
 * is necessary, along with associating a correlation ID for tracking purposes.
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
