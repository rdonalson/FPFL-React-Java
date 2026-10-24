package com.financialplanner.moduleapi.controllers;

import com.financialplanner.moduleapi.mappers.ItemTypeMapper;
import com.financialplanner.moduleapi.mappers.TimePeriodMapper;
import com.financialplanner.moduleapi.support.ApiTestSupport.Result;
import com.financialplanner.modulecommonbc.exception.DuplicateItemException;
import com.financialplanner.modulecommonbc.exception.ItemNotFoundException;
import com.financialplanner.modulecommonbc.sanitizer.SanitizerImpl;
import com.financialplanner.moduleitemsbc.domain.service.ItemTypeService;
import com.financialplanner.moduleitemsbc.domain.service.TimePeriodService;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemType;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.TimePeriod;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.financialplanner.moduleapi.support.ApiTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/** /item-types and /time-periods: the admin catalogs. */
class CatalogControllersTest {

    @Nested
    class ItemTypes {
        private final ItemTypeService service = mock(ItemTypeService.class);
        private final MockMvc mvc = mvc(new ItemTypeController(service, new ItemTypeMapper(new SanitizerImpl()), responses()));

        @Test
        void listsAll() {
            when(service.list()).thenReturn(List.of(new ItemType(1L, "Credit"), new ItemType(2L, "Debit")));
            Result r = call(mvc, get("/item-types"));
            assertEquals(200, r.status());
            assertTrue(r.has("\"name\":\"Credit\"") && r.has("\"name\":\"Debit\""), r.body());
            assertTrue(r.has("ItemTypes fetched successfully"));
        }

        @Test
        void getsOneOr404() {
            when(service.get(1L)).thenReturn(new ItemType(1L, "Credit"));
            when(service.get(9L)).thenThrow(new ItemNotFoundException("ItemType 9 not found"));

            assertEquals(200, call(mvc, get("/item-types/1")).status());
            Result missing = call(mvc, get("/item-types/9"));
            assertEquals(404, missing.status());
            assertTrue(missing.has("ItemType 9 not found"));
            assertTrue(missing.body().matches("(?s).*\"correlationId\":\"[0-9a-f-]{36}\".*"), missing.body());
        }

        @Test
        void createsWith201AndALocation() {
            when(service.create(any())).thenAnswer(inv -> inv.getArgument(0));
            Result r = call(mvc, post("/item-types").contentType(JSON).content("{\"id\":4,\"name\":\"Other\"}"));
            assertEquals(201, r.status());
            assertEquals("/item-types/create/4", r.location());
            assertTrue(r.has("\"status\":201"));
        }

        @Test
        void aDuplicateIs409() {
            when(service.create(any())).thenThrow(new DuplicateItemException("ItemType 1 already exists."));
            Result r = call(mvc, post("/item-types").contentType(JSON).content("{\"id\":1,\"name\":\"Credit\"}"));
            assertEquals(409, r.status());
            assertTrue(r.has("ItemType 1 already exists."));
        }

        @Test
        void aNameWithMarkupIsRejected() {
            Result r = call(mvc, post("/item-types").contentType(JSON).content("{\"id\":5,\"name\":\"<b>bold</b>\"}"));
            assertEquals(400, r.status());
            verify(service, never()).create(any());
        }

        @Test
        void renamesAndDeletes() {
            when(service.update(any())).thenAnswer(inv -> inv.getArgument(0));
            Result r = call(mvc, put("/item-types/4").contentType(JSON).content("{\"name\":\"Misc\"}"));
            assertEquals(200, r.status());
            assertTrue(r.has("\"id\":4") && r.has("\"name\":\"Misc\""), r.body());

            Result d = call(mvc, delete("/item-types/4"));
            assertEquals(200, d.status());
            assertTrue(d.has("ItemType 4 deleted successfully"));
            verify(service).delete(4L);
        }
    }

    @Nested
    class TimePeriods {
        private final TimePeriodService service = mock(TimePeriodService.class);
        private final MockMvc mvc = mvc(new TimePeriodController(service, new TimePeriodMapper(new SanitizerImpl()), responses()));

        @Test
        void listGetCreateUpdateDelete() {
            when(service.list()).thenReturn(List.of(new TimePeriod(6L, "Monthly")));
            when(service.get(6L)).thenReturn(new TimePeriod(6L, "Monthly"));
            when(service.create(any())).thenAnswer(inv -> inv.getArgument(0));
            when(service.update(any())).thenAnswer(inv -> inv.getArgument(0));

            assertTrue(call(mvc, get("/time-periods")).has("Monthly"));
            assertTrue(call(mvc, get("/time-periods/6")).has("\"id\":6"));
            assertEquals(201, call(mvc, post("/time-periods").contentType(JSON).content("{\"id\":11,\"name\":\"Hourly\"}")).status());
            assertTrue(call(mvc, put("/time-periods/11").contentType(JSON).content("{\"name\":\"Every Hour\"}")).has("Every Hour"));
            assertEquals(200, call(mvc, delete("/time-periods/11")).status());
            verify(service).delete(11L);
        }

        @Test
        void missingIs404() {
            when(service.get(99L)).thenThrow(new ItemNotFoundException("TimePeriod 99 not found"));
            assertEquals(404, call(mvc, get("/time-periods/99")).status());
        }
    }
}
