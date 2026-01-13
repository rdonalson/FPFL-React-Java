package com.financialplanner.moduleapi.response;

import com.financialplanner.modulecommonbc.sanitizer.Sanitizer;
import org.springframework.stereotype.Component;

/**
 * A factory class responsible for creating standardized API response objects.
 * This class provides helper methods to construct success, creation, and error responses
 * with a consistent format, making it easier to build and send responses in a uniform manner.
 * <p>
 * It also integrates with a {@link Sanitizer} to sanitize objects before sending responses,
 * ensuring their safety and conformance to the defined requirements.
 */
@Component
public class ApiResponseFactory {

    private final Sanitizer sanitizer;

    public ApiResponseFactory(Sanitizer sanitizer) {
        this.sanitizer = sanitizer;
    }

    public <T> ApiResponse<T> success(T data, String message, String correlationId) {
        return new ApiResponse<>(200, message, data, correlationId);
    }

    public ApiResponse<Void> success(String message, String correlationId) {
        return new ApiResponse<>(200, message, correlationId);
    }

    public <T> ApiResponse<T> created(T data, String message, String correlationId) {
        ApiResponse<T> response = new ApiResponse<>(201, message, data, correlationId);
        // ⭐ Break final tainted flow
        sanitizer.sanitize(response);
        return response;
    }

    public ApiResponse<Void> error(int status, String message, String correlationId) {
        return new ApiResponse<>(status, message, correlationId);
    }
}
