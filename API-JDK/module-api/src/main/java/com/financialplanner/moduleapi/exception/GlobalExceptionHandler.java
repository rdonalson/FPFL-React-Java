package com.financialplanner.moduleapi.exception;

import com.financialplanner.moduleapi.logging.ErrorLogger;
import com.financialplanner.moduleapi.response.ApiResponse;
import com.financialplanner.modulecommonbc.exception.IllegalArgumentException;
import com.financialplanner.modulecommonbc.exception.DuplicateItemException;
import com.financialplanner.modulecommonbc.exception.InvalidRequestException;
import com.financialplanner.modulecommonbc.exception.ItemNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * A global exception handler class that provides centralized handling for
 * exception thrown across the application. This class uses
 *
 * @RestControllerAdvice annotation to intercept and process exception by
 * mapping them to standardized responses.
 * ---
 * The methods in this class handle various types of exception and return
 * a ResponseEntity containing an ApiResponse object. The ApiResponse includes
 * details like the HTTP status code, error message, and a correlation ID
 * for tracking errors in a distributed system.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        String correlationId = ErrorLogger.logException(ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(new ApiResponse<>(400, ex.getMessage(), correlationId));
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(InvalidRequestException ex) {
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

