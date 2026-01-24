package com.financialplanner.moduleapi.exception;

import com.financialplanner.moduleapi.logging.ErrorLogger;
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
 * A centralized exception handler for managing and standardizing API error responses
 * across the application. Exceptions handled by this class will automatically
 * return structured and meaningful HTTP responses to the client, adhering to defined
 * status codes and including additional context when necessary.
 * This class uses Spring's {@code @RestControllerAdvice} and {@code @ExceptionHandler}
 * annotations to define global exception handling mechanisms.
 * Handled Exceptions:
 * - {@link java.lang.IllegalArgumentException}: Returns a 400 Bad Request status code.
 * - {@link DomainValidationException}: Returns a 400 Bad Request status code.
 * - {@link ItemNotFoundException}: Returns a 404 Not Found status code.
 * - {@link DuplicateItemException}: Returns a 409 Conflict status code.
 * - {@link Exception}: Catches all other uncaught exceptions and returns a 500 Internal Server Error.
 * Key Features:
 * - Logs exceptions using the {@code ErrorLogger.logException} method to capture details
 * and generate a unique correlation ID for traceability.
 * - Standardizes API responses by returning an {@code ApiResponse<Void>} object, which includes
 * essential response details such as status, message, and correlation ID.
 * - Ensures a consistent client experience by using meaningful HTTP status codes and descriptive messages.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SanitizationException.class)
    public ResponseEntity<ApiResponse<Void>> handleSanitization(SanitizationException ex) {
        String correlationId = ErrorLogger.logException(ex);
        return ResponseEntity.badRequest()
                             .body(new ApiResponse<>(400, ex.getMessage(), correlationId));
    }

    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomainValidation(DomainValidationException ex) {
        String correlationId = ErrorLogger.logException(ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(new ApiResponse<>(400, ex.getMessage(), correlationId));
    }

//    @ExceptionHandler(IllegalArgumentException.class)
//    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
//        String correlationId = ErrorLogger.logException(ex);
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                             .body(new ApiResponse<>(400, ex.getMessage(), correlationId));
//    }

//    @ExceptionHandler(InvalidRequestException.class)
//    public ResponseEntity<ApiResponse<Void>> handleBadRequest(InvalidRequestException ex) {
//        String correlationId = ErrorLogger.logException(ex);
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                             .body(new ApiResponse<>(400, ex.getMessage(), correlationId));
//    }

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

