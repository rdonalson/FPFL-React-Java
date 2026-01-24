package com.financialplanner.moduleapi.response;

import com.financialplanner.modulecommonbc.sanitizer.Sanitizer;
import org.springframework.stereotype.Component;

/**
 * A factory class for constructing standardized API response objects.
 * This class provides utility methods to create success and created responses
 * with various configurations. All responses are sanitized to ensure data integrity.
 * The factory leverages the {@link Sanitizer} interface for cleaning the response objects
 * before they are returned.
 */
@Component
public class ApiResponseFactory {

    private final Sanitizer sanitizer;

    public ApiResponseFactory(Sanitizer sanitizer) {
        this.sanitizer = sanitizer;
    }

    /**
     * Creates a success {@link ApiResponse} with the provided data and message.
     * The response is initialized with a status code of 200 to indicate success
     * and is sanitized before being returned.
     *
     * @param <T>     the type of the data payload contained in the response
     * @param data    the data to be included in the API response
     * @param message a descriptive message to be included in the API response
     *
     * @return a sanitized {@link ApiResponse} instance containing the status, message, and data
     */
    public <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>(200, message, data);
        sanitizer.sanitize(response);
        return response;
    }

    /**
     * Creates an {@link ApiResponse} object with a status code of 200, indicating a successful response.
     * The provided message is sanitized before being included in the response.
     *
     * @param message a descriptive message to be included in the API response
     *
     * @return an {@link ApiResponse} instance with a status code of 200 and the sanitized message
     */
    public ApiResponse<Void> success(String message) {
        sanitizer.sanitize(message);
        return new ApiResponse<>(200, message);
    }

    /**
     * Creates an {@link ApiResponse} object representing a "Created" HTTP status (201),
     * containing the provided data, message, and correlation ID. The response object
     * is sanitized before being returned.
     *
     * @param <T>           the type of the data payload contained in the response
     * @param data          the payload to include in the response body
     * @param message       a human-readable message describing the response
     * @param correlationId an optional identifier used to track the request/response
     *
     * @return an {@link ApiResponse} object with a 201 status code, the specified message,
     * data payload, and correlation ID
     */
    public <T> ApiResponse<T> created(T data, String message, String correlationId) {
        ApiResponse<T> response = new ApiResponse<>(201, message, data, correlationId);
        sanitizer.sanitize(response);
        return response;
    }
}
