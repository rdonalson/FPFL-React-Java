package com.financialplanner.moduleapi.exception;

import com.financialplanner.moduleapi.logging.ErrorLogger;
import com.financialplanner.moduleapi.response.ApiResponse;
import com.financialplanner.moduleitemsbc.domain.exception.DuplicateItemException;
import com.financialplanner.moduleitemsbc.domain.exception.InvalidRequestException;
import com.financialplanner.moduleitemsbc.domain.exception.ItemNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * A global exception handler for standardizing error responses across the application.
 * ---
 * This class is annotated with {@code @RestControllerAdvice} to intercept exceptions thrown
 * by the controllers and provide centralized exception-handling logic. The methods in this
 * class generate uniform API responses containing the error code, a descriptive error
 * message, and a correlation ID for traceability.
 * ---
 * The exception-handling methods utilize {@code @ExceptionHandler} annotations to define
 * specific behaviors for handling different types of exceptions, such as invalid requests,
 * resource not found, duplication conflicts, or generic server errors.
 * ---
 * Each handler logs the exception and assigns a unique correlation ID using the {@code ErrorLogger}
 * utility. This correlation ID enables easier debugging and tracking of errors occurring
 * in the system. All responses follow a consistent structure using the {@code ApiResponse}
 * class for better client-side handling.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles exceptions of type {@code InvalidRequestException} by returning a standardized
     * response with a 400 Bad Request status code.
     *
     * The method logs the exception to generate a correlation ID, which is helpful for
     * tracing and debugging purposes. It then constructs an {@code ApiResponse<Void>}
     * object that contains details about the error status, message, and correlation ID.
     *
     * @param ex the {@code InvalidRequestException} to be handled; contains details about
     *           the invalid request.
     * @return a {@code ResponseEntity} containing an {@code ApiResponse<Void>} object with
     *         a 400 status code, a descriptive error message, and a correlation ID.
     */
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(InvalidRequestException ex) {
        String correlationId = ErrorLogger.logException(ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(new ApiResponse<>(400, ex.getMessage(), correlationId));
    }

    /**
     * Handles {@link ItemNotFoundException} by logging the exception and returning a standardized response.
     * ---
     * This method is triggered when an {@code ItemNotFoundException} is thrown within the application context.
     * It constructs a {@link ResponseEntity} containing an {@link ApiResponse} with a 404 Not Found status,
     * the exception's message, and a correlation ID for tracking purposes.
     *
     * @param ex the {@code ItemNotFoundException} that was thrown
     * @return a {@code ResponseEntity} containing an {@code ApiResponse} with a 404 status, the exception message,
     *         and a correlation ID for debugging
     */
    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleItemNotFound(ItemNotFoundException ex) {
        String correlationId = ErrorLogger.logException(ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(new ApiResponse<>(404, ex.getMessage(), correlationId));
    }


    /**
     * Handles exceptions of type {@code DuplicateItemException} and generates
     * an appropriate HTTP {@code 409 Conflict} response.
     * ---
     * When a {@code DuplicateItemException} is thrown, this method logs the exception,
     * assigns a correlation ID, and returns a standardized API response encapsulating
     * error details including the status code, descriptive message, and correlation ID.
     * ---
     * @param ex the exception thrown when a duplicate item is detected, containing
     *           details about the error
     * @return a {@code ResponseEntity} containing an {@code ApiResponse<Void>} object
     *         with a standardized error message, HTTP status {@code 409 Conflict},
     *         and a correlation ID for debugging purposes
     */
    @ExceptionHandler(DuplicateItemException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(DuplicateItemException ex) {
        String correlationId = ErrorLogger.logException(ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                             .body(new ApiResponse<>(409, ex.getMessage(), correlationId));
    }

    /**
     * Handles any generic exception thrown within the application and returns a standardized
     * API response with an HTTP status of 500 (Internal Server Error).
     * The method also logs the exception details with a generated correlation ID for tracking.
     * ---
     * @param ex the exception that was thrown; must not be null
     * @return a ResponseEntity containing an ApiResponse object with the error details, including
     *         the HTTP status code, error message, and a unique correlation ID
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        String correlationId = ErrorLogger.logException(ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(new ApiResponse<>(500, ex.getMessage(), null, correlationId));
    }
}

