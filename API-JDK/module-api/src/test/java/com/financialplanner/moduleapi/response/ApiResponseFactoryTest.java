package com.financialplanner.moduleapi.response;

import com.financialplanner.modulecommonbc.exception.SanitizationException;
import com.financialplanner.modulecommonbc.sanitizer.SanitizerImpl;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ApiResponseFactoryTest {

    private final ApiResponseFactory factory = new ApiResponseFactory(new SanitizerImpl());

    /** Public, like the API's own response records: the sanitizer rebuilds records through reflection. */
    public record Row(String name) {}

    @Test
    void successWrapsTheDataWith200AndSanitizesIt() {
        ApiResponse<List<Row>> res = factory.success(new ArrayList<>(List.of(new Row("  Rent  "))), "Items fetched successfully");

        assertEquals(200, res.getStatus());
        assertEquals("Items fetched successfully", res.getMessage());
        assertEquals("Rent", res.getData().getFirst().name());
        assertEquals("", res.getCorrelationId());
        assertFalse(res.getTimestamp().isAfter(Instant.now()));
    }

    @Test
    void createdCarriesTheLocationAs201() {
        ApiResponse<Row> res = factory.created(new Row("Rent"), "Item created successfully", "/items/5");
        assertEquals(201, res.getStatus());
        assertEquals("/items/5", res.getCorrelationId());
    }

    @Test
    void messageOnlyResponses() {
        ApiResponse<Void> res = factory.success("Item 5 deleted successfully");
        assertEquals(200, res.getStatus());
        assertNull(res.getData());
    }

    @Test
    void unsafeDataInAResponseIsRefused() {
        assertThrows(SanitizationException.class,
                     () -> factory.success(new ArrayList<>(List.of(new Row("<script>"))), "x"));
    }

    @Test
    void constructorsFillTheRightFields() {
        assertEquals("c-1", new ApiResponse<Void>(404, "Not found", "c-1").getCorrelationId());
        assertEquals("", new ApiResponse<Void>(200, "ok").getCorrelationId());
        assertEquals("d", new ApiResponse<>(500, "m", "d", "c").getData());
    }
}
