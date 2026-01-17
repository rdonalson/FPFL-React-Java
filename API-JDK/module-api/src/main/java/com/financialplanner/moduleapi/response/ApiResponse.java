package com.financialplanner.moduleapi.response;

import java.time.Instant;

/**
 * Represents a standardized API response structure used to encapsulate
 * response details such as status, message, data payload, timestamp, and an optional correlation ID.
 * This class can be used to send both success and error responses across an application.
 *
 * @param <T> the type of the data payload contained in the response
 */
@lombok.Data
public class ApiResponse<T> {

    private int status;
    private String message;
    private T data;
    private Instant timestamp;
    private String correlationId; // optional, for debugging

    public ApiResponse(int status, String message, T data, String correlationId) {
        this.status        = status;
        this.message       = message;
        this.data          = data;
        this.correlationId = correlationId;
        this.timestamp     = Instant.now();
    }

    public ApiResponse(int status, String message, String correlationId) {
        this(status, message, null, correlationId);
    }
}
