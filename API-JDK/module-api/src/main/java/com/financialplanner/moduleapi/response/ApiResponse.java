package com.financialplanner.moduleapi.response;

import java.time.Instant;

/**
 * A generic class representing a standardized structure for API responses.
 * It is used to encapsulate details such as the response status, message,
 * data payload, timestamp, and an optional correlation ID for debugging purposes.
 *
 * @param <T> the type of the data payload included in the response, allowing flexibility
 *            to accommodate various types of response content.
 */
@lombok.Data
public class ApiResponse<T> {

    /**
     * Represents the status code of the API response.
     * This field typically indicates the outcome of a request, where standard HTTP status
     * codes may be used (e.g., 200 for success, 400 for client error, 500 for server error).
     */
    private int status;
    /**
     * Represents a descriptive message included in the API response.
     * This message typically provides additional context or information
     * regarding the response status or outcome.
     */
    private String message;
    /**
     * Represents the payload or body of the API response.
     * This is a generic type, allowing flexibility for the structure
     * of the response content.
     */
    private T data;
    /**
     * The timestamp indicating when the response was created.
     * It is automatically set to the current time when the object is instantiated.
     * Useful for tracking the time of the response generation.
     */
    private Instant timestamp;
    /**
     * An optional identifier used to correlate a specific API request and response.
     * Primarily intended for debugging and tracking purposes.
     */
    private String correlationId; // optional, for debugging

    /**
     * Default no-argument constructor for ApiResponse.
     * This constructor initializes the timestamp field to the current time.
     * It is required for deserialization frameworks such as Jackson.
     */
    public ApiResponse() {
        // Required for Jackson
        this.timestamp = Instant.now();
    }

    /**
     * Constructs an instance of the ApiResponse class with the specified status, message, data, and correlation ID.
     * This constructor automatically sets the timestamp to the current time.
     *
     * @param status        the HTTP status code associated with the response
     * @param message       a textual message describing the response
     * @param data          the payload of the response, typically containing the requested data
     * @param correlationId an optional correlation ID for debugging and tracing purposes
     */
    public ApiResponse(int status, String message, T data, String correlationId) {
        this.status        = status;
        this.message       = message;
        this.data          = data;
        this.correlationId = correlationId;
        this.timestamp     = Instant.now();
    }

    /**
     * Constructs an ApiResponse object without a data payload.
     * This constructor is a convenience method for creating an ApiResponse
     * when no additional payload data is required, typically used for
     * responses that consist only of a status code, message, and optional correlation ID.
     *
     * @param status the HTTP status code representing the result of the operation
     * @param message a descriptive message providing additional context about the response
     * @param correlationId an optional unique identifier for correlating logs or debugging information
     */
   public ApiResponse(int status, String message, String correlationId) {
        this(status, message, null, correlationId);
    }
}
