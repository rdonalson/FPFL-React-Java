package com.financialplanner.moduleapi.exception;


import com.financialplanner.modulecommonbc.logging.ErrorLogger;
import com.financialplanner.moduleapi.response.ApiResponse;
import com.financialplanner.modulecommonbc.exception.DomainValidationException;
import com.financialplanner.modulecommonbc.exception.DuplicateItemException;
import com.financialplanner.modulecommonbc.exception.ItemNotFoundException;
import com.financialplanner.modulecommonbc.exception.SanitizationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * A centralized global exception handler for handling exceptions across the application.
 * This class uses Spring's {@code @RestControllerAdvice} and {@code @ExceptionHandler}
 * annotations to intercept and manage exceptions, providing consistent API responses
 * with appropriate HTTP status codes and error details.
 * This handler generates a unique correlation ID for each logged exception using the
 * {@link ErrorLogger} utility, enabling enhanced traceability of issues.
 * Key Features:
 * - Handles specific exceptions such as:
 * - {@link SanitizationException}: Indicates issues with input data sanitization.
 * - {@link DomainValidationException}: Identifies domain-specific validation errors.
 * - {@link ItemNotFoundException}: Represents scenarios where a required item is missing.
 * - {@link DuplicateItemException}: Indicates conflicts due to duplicate resources.
 * - Provides a generic handler for all other uncaught exceptions.
 * - Returns a standardized API response encapsulated in {@link ApiResponse}.
 * - Assigns appropriate HTTP status codes (e.g., 400, 404, 409, 500) based on the exception type.
 * Intended Usage:
 * - Use this class to ensure consistent error handling across all RESTful endpoints.
 * - Log all exceptions with a unique correlation ID to facilitate tracking and debugging.
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

