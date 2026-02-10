package com.financialplanner.moduleapi.dtos.logging;

@lombok.Data
public class ClientLogRequest {
    private String level;          // "info", "warn", "error"
    private String url;            // API endpoint that failed
    private Integer status;        // HTTP status code
    private String message;        // Error message
    private String correlationId;  // X-Correlation-ID from frontend
    private Object details;        // Optional extra info (stack, payload)

    public ClientLogRequest() {}
}
