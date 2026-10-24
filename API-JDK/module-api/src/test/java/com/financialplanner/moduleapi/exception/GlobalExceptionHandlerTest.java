package com.financialplanner.moduleapi.exception;

import com.financialplanner.moduleapi.response.ApiResponse;
import com.financialplanner.modulecommonbc.exception.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.junit.jupiter.api.Assertions.*;

/** Every exception the API knows about becomes the right status, a readable message and a correlation id. */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private static void assertResponse(ResponseEntity<ApiResponse<Void>> res, int status, String message) {
        assertEquals(status, res.getStatusCode().value());
        assertNotNull(res.getBody());
        assertEquals(status, res.getBody().getStatus());
        assertEquals(message, res.getBody().getMessage());
        assertNotNull(res.getBody().getCorrelationId());
        assertFalse(res.getBody().getCorrelationId().isBlank());
    }

    @Test
    void domainExceptionsMapToTheirStatus() {
        assertResponse(handler.handleSanitization(new SanitizationException("Invalid characters in string")), 400, "Invalid characters in string");
        assertResponse(handler.handleDomainValidation(new DomainValidationException("Name is required")), 400, "Name is required");
        assertResponse(handler.handleItemNotFound(new ItemNotFoundException("Item 4 not found")), 404, "Item 4 not found");
        assertResponse(handler.handleConflict(new DuplicateItemException("already exists")), 409, "already exists");
        assertResponse(handler.handleInvalidCredentials(new InvalidCredentialsException("Invalid credentials")), 401, "Invalid credentials");
        assertResponse(handler.ForbiddenOperation(new ForbiddenOperationException("not yours")), 403, "not yours");
    }

    @Test
    void anythingElseIs500() {
        assertResponse(handler.handleGeneric(new IllegalStateException("boom")), 500, "boom");
        assertResponse(handler.handleGeneric(new RepositoryException("Database failure", new RuntimeException())), 500, "Database failure");
    }

    @Test
    void anUnreadableBodyIs400WithTheRealReason() {
        var wrapped = new HttpMessageNotReadableException("JSON parse error",
                                                          new RuntimeException(new DomainValidationException("Item Name is required")),
                                                          new MockHttpInputMessage(new byte[0]));
        assertResponse(handler.handleUnreadableBody(wrapped), 400, "Item Name is required");

        var garbage = new HttpMessageNotReadableException("JSON parse error", new MockHttpInputMessage(new byte[0]));
        assertResponse(handler.handleUnreadableBody(garbage), 400, "Request body is missing or malformed");
    }

    @Test
    void unknownPathsAndWrongMethods() {
        assertResponse(handler.handleNoResource(new NoResourceFoundException(HttpMethod.GET, "/nope", "nope")), 404, "Not found");
        assertEquals(405, handler.handleMethodNotAllowed(new HttpRequestMethodNotSupportedException("PATCH")).getStatusCode().value());
    }
}
