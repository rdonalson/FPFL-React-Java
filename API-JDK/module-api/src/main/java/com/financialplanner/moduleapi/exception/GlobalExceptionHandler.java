package com.financialplanner.moduleapi.exception;


import com.financialplanner.moduleapi.response.ApiResponse;
import com.financialplanner.modulecommonbc.exception.DomainValidationException;
import com.financialplanner.modulecommonbc.exception.DuplicateItemException;
import com.financialplanner.modulecommonbc.exception.ItemNotFoundException;
import com.financialplanner.modulecommonbc.exception.SanitizationException;
import com.financialplanner.modulecommonbc.logging.ErrorLogger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler to manage and standardize exception responses across
 * the application. This class leverages the Spring {@code @RestControllerAdvice}
 * annotation to intercept and handle exceptions thrown by controller methods,
 * mapping them to appropriate HTTP responses with structured payloads.
 * The exception responses are encapsulated in {@link ApiResponse} objects, providing
 * relevant details such as HTTP status, error message, optional correlation ID, and
 * timestamp. Additionally, exceptions are logged with a unique correlation ID for
 * traceability using {@link ErrorLogger}.
 * Supported exception types and mapped HTTP status codes:
 * - {@link SanitizationException}: Returns HTTP 400 (Bad Request).
 * - {@link DomainValidationException}: Returns HTTP 400 (Bad Request).
 * - {@link ItemNotFoundException}: Returns HTTP 404 (Not Found).
 * - {@link DuplicateItemException}: Returns HTTP 409 (Conflict).
 * - {@link Exception} (generic case): Returns HTTP 500 (Internal Server Error).
 * Each handler logs the exception using {@code ErrorLogger.logException(Exception ex)},
 * captures the generated correlation ID, and includes it in the response body for
 * enhanced debugging and error tracking.
 * Thread Safety:
 * - This class is thread-safe as exception handling methods are stateless and rely
 * on thread-safe components such as {@code ErrorLogger}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SanitizationException.class)
    public ResponseEntity<ApiResponse<Void>> handleSanitization(SanitizationException ex) {
        String correlationId = ErrorLogger.logException(ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(new ApiResponse<>(400, ex.getMessage(), correlationId));
    }

    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomainValidation(DomainValidationException ex) {
        String correlationId = ErrorLogger.logException(ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(new ApiResponse<>(400, ex.getMessage(), correlationId));
    }

    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleItemNotFound(ItemNotFoundException ex) {
        String correlationId = ErrorLogger.logException(ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(new ApiResponse<>(404, ex.getMessage(), correlationId));
    }

    @ExceptionHandler(DuplicateItemException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(DuplicateItemException ex) {
        String correlationId = ErrorLogger.logException(ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                             .body(new ApiResponse<>(409, ex.getMessage(), correlationId));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        String correlationId = ErrorLogger.logException(ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(new ApiResponse<>(500, ex.getMessage(), null, correlationId));
    }
}

