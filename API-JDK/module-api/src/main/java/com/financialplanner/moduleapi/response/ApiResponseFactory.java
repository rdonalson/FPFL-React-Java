package com.financialplanner.moduleapi.response;

import com.financialplanner.modulecommonbc.sanitizer.Sanitizer;
import org.springframework.stereotype.Component;

/**
 * Factory class for creating standardized API response objects encapsulated in {@link ApiResponse}.
 * This class provides methods to generate success, created, and error responses, ensuring
 * proper response structure for API communication. It also integrates the use of a {@link Sanitizer}
 * to sanitize the response objects before returning them.
 */
@Component
public class ApiResponseFactory {

    private final Sanitizer sanitizer;

    public ApiResponseFactory(Sanitizer sanitizer) {
        this.sanitizer = sanitizer;
    }

    public <T> ApiResponse<T> success(T data, String message, String correlationId) {
        ApiResponse<T> response = new ApiResponse<>(200, message, data, correlationId);
        sanitizer.sanitize(response);
        return response;
    }

    public ApiResponse<Void> success(String message, String correlationId) {
        return new ApiResponse<>(200, message, correlationId);
    }

    public <T> ApiResponse<T> created(T data, String message, String correlationId) {
        ApiResponse<T> response = new ApiResponse<>(201, message, data, correlationId);
        sanitizer.sanitize(response);
        return response;
    }

    public ApiResponse<Void> error(int status, String message, String correlationId) {
        return new ApiResponse<>(status, message, correlationId);
    }
}
