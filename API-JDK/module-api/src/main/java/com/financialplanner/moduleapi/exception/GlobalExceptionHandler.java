package com.financialplanner.moduleapi.exception;


import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import com.financialplanner.moduleapi.response.ApiResponse;
import com.financialplanner.modulecommonbc.exception.*;
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

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(InvalidCredentialsException ex) {
        String correlationId = ErrorLogger.logException(ex);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                             .body(new ApiResponse<>(401, ex.getMessage(), correlationId));
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<ApiResponse<Void>> ForbiddenOperation(ForbiddenOperationException ex) {
        String correlationId = ErrorLogger.logException(ex);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                             .body(new ApiResponse<>(403, ex.getMessage(), correlationId));
    }

    /**
     * A request body that fails its {@code @Valid} constraints (e.g. a malformed email or a short password)
     * is the client's mistake: 400 with the field messages, not a 500 with Spring's internal description.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidBody(MethodArgumentNotValidException ex) {
        String correlationId = ErrorLogger.logException(ex);
        String message = ex.getBindingResult()
                           .getFieldErrors()
                           .stream()
                           .map(e -> e.getDefaultMessage() != null ? e.getDefaultMessage() : e.getField() + " is invalid")
                           .sorted()
                           .distinct()
                           .reduce((a, b) -> a + "; " + b)
                           .orElse("Request is invalid");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(new ApiResponse<>(400, message, correlationId));
    }

    /**
     * A body that can't be read: bad JSON, a wrong type, or a request record whose constructor rejected it
     * (those throw {@link DomainValidationException}, which arrives wrapped). 400 with the real reason.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadableBody(HttpMessageNotReadableException ex) {
        String correlationId = ErrorLogger.logException(ex);
        String message = "Request body is missing or malformed";
        for (Throwable t = ex; t != null; t = t.getCause()) {
            if (t instanceof DomainValidationException || t instanceof SanitizationException) {
                message = t.getMessage();
                break;
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(new ApiResponse<>(400, message, correlationId));
    }

    /** A path or query value of the wrong type, e.g. {@code /items/abc}. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String correlationId = ErrorLogger.logException(ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(new ApiResponse<>(400, "Invalid value for " + ex.getName(), correlationId));
    }

    /** No such endpoint: 404 rather than 500. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException ex) {
        String correlationId = ErrorLogger.logException(ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(new ApiResponse<>(404, "Not found", correlationId));
    }

    /** The endpoint exists but not for this HTTP method: 405. */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        String correlationId = ErrorLogger.logException(ex);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                             .body(new ApiResponse<>(405, ex.getMessage(), correlationId));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        String correlationId = ErrorLogger.logException(ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(new ApiResponse<>(500, ex.getMessage(), null, correlationId));
    }
}

