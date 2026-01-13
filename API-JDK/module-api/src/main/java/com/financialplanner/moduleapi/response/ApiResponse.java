package com.financialplanner.moduleapi.response;

import java.time.Instant;

/**
 * A generic class representing a standardized API response structure. This class is designed to
 * encapsulate the common elements of an API response, such as the HTTP status code, a descriptive
 * message, the payload (optional), a timestamp, and an optional correlation ID for debugging or tracing.
 *
 * @param <T> the type of the data payload included in the API response.
 */
@lombok.Data
public class ApiResponse<T> {

    /**
     * Represents the HTTP status code associated with the API response.
     * The status code provides a standardized way to indicate the
     * result of the operation, such as success (e.g., 200 for OK),
     * creation (e.g., 201 for Created), or an error (e.g., 400 for Bad Request).
     */
    private int status;
    /**
     * A descriptive message providing details about the API response.
     * It is intended to convey information such as the outcome of an operation,
     * potential errors, or any other meaningful context relevant to the response.
     */
    private String message;
    /**
     * Represents the main payload or body of the API response.
     * This field is a generic type, allowing the inclusion of any type of
     * object as the data payload in the response. It can represent additional
     * details, results, or specific information depending on the context of the API call.
     * ---
     *
     * @param <T> the type of the data that this field holds, enabling flexibility for
     * various data formats or structures.
     */
    private T data;
    /**
     * The timestamp representing the exact moment when the response was created or initialized.
     * It is automatically set to the current instant (UTC) during the construction of the
     * {@code ApiResponse} object.
     * ---
     * This field is useful for tracking and logging purposes, as it can be used to determine
     * when the API response was generated.
     */
    private Instant timestamp;
    /**
     * Represents an optional identifier used to trace and correlate specific API request-response
     * interactions, primarily for debugging purposes. This value can help identify and debug
     * issues by associating the current response with a corresponding request or workflow.
     * ---
     * Example scenarios where this field might be useful include:
     * - Log analysis: Correlation ID can be included in logs for better traceability.
     * - Distributed systems: Helps track and associate actions across multiple services.
     * - Debugging: Simplifies tracking the flow of specific requests and their corresponding responses.
     * ---
     * This field is optional and may be left null if no correlation ID is applicable or required.
     */
    private String correlationId; // optional, for debugging

    /**
     * Default no-argument constructor for the ApiResponse class.
     * Initializes the timestamp field to the current instant in time.
     * ---
     * This constructor is specifically required for object mapping libraries like Jackson
     * to create an instance of the class during deserialization.
     */
    public ApiResponse() {
        // Required for Jackson
        this.timestamp = Instant.now();
    }

    /**
     * Constructs an instance of {@code ApiResponse} with the specified status, message,
     * data payload, and correlation ID. The timestamp is set to the current instant.
     * ---
     *
     * @param status        the HTTP status code representing the outcome of the API request
     * @param message       a descriptive message providing additional context about the API response
     * @param data          the payload containing the response data; can be of any type or {@code null}
     * @param correlationId an optional identifier for tracking and debugging purposes
     */
    public ApiResponse(int status, String message, T data, String correlationId) {
        this.status        = status;
        this.message       = message;
        this.data          = data;
        this.correlationId = correlationId;
        this.timestamp     = Instant.now();
    }

    /**
     * Constructs an instance of {@code ApiResponse} with the specified status, message,
     * and correlation ID. The data payload is initialized to {@code null}.
     * ---
     *
     * @param status        the HTTP status code representing the outcome of the API operation
     * @param message       the message describing the response or providing additional details
     * @param correlationId the optional identifier used for tracing and debugging purposes
     */
    public ApiResponse(int status, String message, String correlationId) {
        this(status, message, null, correlationId);
    }
}
