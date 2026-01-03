package com.financialplanner.moduleapi.response;

import java.time.Instant;

@lombok.Data
public class ApiResponse<T> {

    private int status;
    private String message;
    private T data;
    private Instant timestamp;
    private String correlationId; // optional, for debugging

    public ApiResponse() {
        // Required for Jackson
        this.timestamp = Instant.now();
    }

    public ApiResponse(int status,
                       String message,
                       T data,
                       String correlationId
                      ) {
        this.status        = status;
        this.message       = message;
        this.data          = data;
        this.correlationId = correlationId;
        this.timestamp     = Instant.now();
    }

    // Convenience constructor for responses without data
    public ApiResponse(int status,
                       String message,
                       String correlationId
                      ) {
        this(status,
             message,
             null,
             correlationId
            );
    }
}
