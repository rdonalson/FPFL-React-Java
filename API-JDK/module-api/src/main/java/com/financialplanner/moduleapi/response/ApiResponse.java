package com.financialplanner.moduleapi.response;

import java.time.Instant;

@lombok.Data
public class ApiResponse<T> {

    private int status;
    private String message;
    private T data;
    private Instant timestamp;

    public ApiResponse() {
        // Required for Jackson
        this.timestamp = Instant.now();
    }

    public ApiResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = Instant.now();
    }

    // Convenience constructor for responses without data
    public ApiResponse(int status, String message) {
        this(status, message, null);
    }
}
