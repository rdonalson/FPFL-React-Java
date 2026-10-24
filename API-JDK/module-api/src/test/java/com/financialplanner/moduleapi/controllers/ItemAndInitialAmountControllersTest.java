package com.financialplanner.moduleapi.controllers;

import com.financialplanner.moduleapi.mappers.InitialAmountMapper;
import com.financialplanner.moduleapi.mappers.ItemMapper;
import com.financialplanner.moduleapi.support.ApiTestSupport.Result;
import com.financialplanner.modulecommonbc.exception.ItemNotFoundException;
import com.financialplanner.modulecommonbc.sanitizer.SanitizerImpl;
import com.financialplanner.moduleitemsbc.domain.repository.ItemTypeRepository;
import com.financialplanner.moduleitemsbc.domain.repository.TimePeriodRepository;
import com.financialplanner.moduleitemsbc.domain.service.ItemService;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemType;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.TimePeriod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.financialplanner.moduleapi.support.ApiTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

class ItemAndInitialAmountControllersTest {

    private static final UUID USER = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final UUID SOMEONE_ELSE = UUID.fromString("99999999-8888-7777-6666-555555555555");

    private final ItemService items = mock(ItemService.class);
    private final ItemTypeRepository itemTypes = mock(ItemTypeRepository.class);
    private final TimePeriodRepository periods = mock(TimePeriodRepository.class);

    @BeforeEach
    void catalogs() {
        when(itemTypes.getReferenceById(anyLong())).thenAnswer(inv -> new ItemType(inv.getArgument(0), "Type"));
        when(periods.findById(6L)).thenReturn(Optional.of(new TimePeriod(6L, "Monthly")));
        // Unless a test says otherwise, the caller is USER
        signIn(USER, "ROLE_USER");
    }

    @AfterEach
    void signedOut() {
        signOut();
    }

    private static Item rent() {
        Item item = new Item();
        item.setId(5L);
        item.setUserId(USER);
        item.setName("Rent");
        item.setAmount(1600.0);
        item.setItemType(new ItemType(2L, "Debit"));
        item.setTimePeriod(new TimePeriod(6L, "Monthly"));
        item.setMonthlyDom(1);
        return item;
    }

    private static final String RENT_JSON =
        "{\"userId\":\"" + USER + "\",\"name\":\"Rent\",\"amount\":1600,\"fkItemType\":2,\"fkPeriod\":6,\"monthlyDom\":1,\"dateRangeReq\":false}";

    @Nested
    class Items {
        private final MockMvc mvc = mvc(new ItemController(items, new ItemMapper(new SanitizerImpl(), itemTypes, periods), responses()));

        @Test
        void listsAUsersItemsOfAType() {
            when(items.findByUserIdAndItemTypeId(USER, 2L)).thenReturn(List.of(rent()));
            Result r = call(mvc, get("/items/" + USER + "/2"));
            assertEquals(200, r.status());
            assertTrue(r.has("\"name\":\"Rent\"") && r.has("\"monthlyDom\":1"), r.body());
        }

        @Test
        void getsOneOr404() {
            when(items.get(5L)).thenReturn(rent());
            when(items.get(6L)).thenThrow(new ItemNotFoundException("Item 6 not found"));
            assertEquals(200, call(mvc, get("/items/5")).status());
            assertEquals(404, call(mvc, get("/items/6")).status());
        }

        @Test
        void createsFromTheRequest() {
            when(items.create(any())).thenAnswer(inv -> {
                Item i = inv.getArgument(0);
                i.setId(5L);
                return i;
            });

            Result r = call(mvc, post("/items").contentType(JSON).content(RENT_JSON));

            assertEquals(201, r.status(), r.body());
            ArgumentCaptor<Item> saved = ArgumentCaptor.forClass(Item.class);
            verify(items).create(saved.capture());
            assertEquals("Rent", saved.getValue().getName());
            assertEquals(6L, saved.getValue().getTimePeriod().getId());
            assertEquals(2L, saved.getValue().getItemType().getId());
        }

        @Test
        void invalidItemsAre400WithTheReason() {
            Result noName = call(mvc, post("/items").contentType(JSON).content(RENT_JSON.replace("\"Rent\"", "\"  \"")));
            assertEquals(400, noName.status(), noName.body());
            assertTrue(noName.has("Item Name is required"), noName.body());

            Result badPeriod = call(mvc, post("/items").contentType(JSON).content(RENT_JSON.replace("\"fkPeriod\":6", "\"fkPeriod\":12")));
            assertEquals(400, badPeriod.status());
            assertTrue(badPeriod.has("Time Period must be between 1 and 10"));

            Result negative = call(mvc, post("/items").contentType(JSON).content(RENT_JSON.replace("1600", "-5")));
            assertEquals(400, negative.status());

            Result markup = call(mvc, post("/items").contentType(JSON).content(RENT_JSON.replace("\"Rent\"", "\"<script>\"")));
            assertEquals(400, markup.status());

            Result broken = call(mvc, post("/items").contentType(JSON).content("{not json"));
            assertEquals(400, broken.status());
            verify(items, never()).create(any());
        }

        @Test
        void updatesAndDeletes() {
            when(items.get(5L)).thenReturn(rent());
            when(items.update(eq(5L), any())).thenAnswer(inv -> {
                Item i = inv.getArgument(1);
                i.setId(5L);
                return i;
            });
            Result r = call(mvc, put("/items/5").contentType(JSON).content(RENT_JSON.replace("1600", "1700")));
            assertEquals(200, r.status(), r.body());
            assertTrue(r.has("\"amount\":1700"));

            assertTrue(call(mvc, delete("/items/5")).has("Item 5 deleted successfully"));
            verify(items).delete(5L);
        }

        @Test
        void listingEveryonesItemsIsForAdminsOnly() {
            when(items.list()).thenReturn(List.of(rent()));
            Result r = call(mvc, get("/items"));
            assertEquals(403, r.status());
            verify(items, never()).list();

            signIn(USER, "ROLE_USER", "ROLE_ADMIN");
            assertEquals(200, call(mvc, get("/items")).status());

            signOut();
            assertEquals(401, call(mvc, get("/items")).status());
        }

        @Test
        void someoneElsesItemLooksMissing() {
            Item theirs = rent();
            theirs.setUserId(SOMEONE_ELSE);
            when(items.get(5L)).thenReturn(theirs);

            assertEquals(404, call(mvc, get("/items/5")).status());
            assertEquals(404, call(mvc, put("/items/5").contentType(JSON).content(RENT_JSON)).status());
            assertEquals(404, call(mvc, delete("/items/5")).status());
            verify(items, never()).update(anyLong(), any());
            verify(items, never()).delete(anyLong());

            signIn(USER, "ROLE_USER", "ROLE_ADMIN");
            assertEquals(200, call(mvc, get("/items/5")).status());
            assertEquals(200, call(mvc, delete("/items/5")).status());
        }

        @Test
        void youCannotListAnotherUsersItems() {
            Result r = call(mvc, get("/items/" + SOMEONE_ELSE + "/2"));
            assertEquals(403, r.status());
            assertTrue(r.has("You can only access your own data"));
            verify(items, never()).findByUserIdAndItemTypeId(any(), anyLong());
        }

        @Test
        void youCannotCreateAnItemForAnotherUser() {
            Result r = call(mvc, post("/items").contentType(JSON).content(RENT_JSON.replace(USER.toString(), SOMEONE_ELSE.toString())));
            assertEquals(403, r.status());
            verify(items, never()).create(any());
        }

        @Test
        void anItemCannotBeHandedToAnotherUser() {
            when(items.get(5L)).thenReturn(rent());
            Result r = call(mvc, put("/items/5").contentType(JSON).content(RENT_JSON.replace(USER.toString(), SOMEONE_ELSE.toString())));
            assertEquals(403, r.status());
            assertTrue(r.has("An item cannot be moved to another user"));
            verify(items, never()).update(anyLong(), any());
        }

        @Test
        void aNonNumericIdIs400() {
            assertEquals(400, call(mvc, get("/items/abc")).status());
        }
    }

    @Nested
    class InitialAmount {
        private final MockMvc mvc = mvc(new InitialAmountController(items, new InitialAmountMapper(new SanitizerImpl(), itemTypes), responses()));

        private Item ia(long id, double amount) {
            Item item = new Item(id, USER, "IA", amount, new ItemType(3L, "Initial Amount"), LocalDate.of(2026, 1, 1));
            return item;
        }

        @Test
        void getsTheUsersInitialAmountOr404() {
            when(items.findByUserIdAndItemTypeId(USER, 3L)).thenReturn(List.of(ia(9, 250)));
            Result r = call(mvc, get("/initial-amount/" + USER));
            assertEquals(200, r.status());
            assertTrue(r.has("\"amount\":250"));

            when(items.findByUserIdAndItemTypeId(USER, 3L)).thenReturn(List.of());
            assertEquals(404, call(mvc, get("/initial-amount/" + USER)).status());
        }

        @Test
        void createsOnlyOne() {
            when(items.findByUserIdAndItemTypeId(USER, 3L)).thenReturn(List.of());
            when(items.create(any())).thenAnswer(inv -> {
                Item i = inv.getArgument(0);
                i.setId(9L);
                return i;
            });
            Result created = call(mvc, post("/initial-amount").contentType(JSON).content("{\"userId\":\"" + USER + "\",\"amount\":0}"));
            assertEquals(201, created.status(), created.body());
            assertEquals("/initial-amount/9", created.location());

            when(items.findByUserIdAndItemTypeId(USER, 3L)).thenReturn(List.of(ia(9, 0)));
            assertEquals(409, call(mvc, post("/initial-amount").contentType(JSON).content("{\"userId\":\"" + USER + "\",\"amount\":5}")).status());
        }

        @Test
        void updatesOnlyTheUsersOwnInitialAmount() {
            when(items.findByUserIdAndItemTypeId(USER, 3L)).thenReturn(List.of(ia(9, 0)));
            when(items.update(eq(9L), any())).thenAnswer(inv -> {
                Item i = inv.getArgument(1);
                i.setId(9L);
                return i;
            });

            assertEquals(200, call(mvc, put("/initial-amount/9").contentType(JSON).content("{\"userId\":\"" + USER + "\",\"amount\":300}")).status());
            assertEquals(404, call(mvc, put("/initial-amount/10").contentType(JSON).content("{\"userId\":\"" + USER + "\",\"amount\":300}")).status());

            when(items.findByUserIdAndItemTypeId(USER, 3L)).thenReturn(List.of());
            assertEquals(404, call(mvc, put("/initial-amount/9").contentType(JSON).content("{\"userId\":\"" + USER + "\",\"amount\":300}")).status());
        }

        @Test
        void anotherUsersInitialAmountIsOffLimits() {
            String theirs = "{\"userId\":\"" + SOMEONE_ELSE + "\",\"amount\":300}";
            assertEquals(403, call(mvc, get("/initial-amount/" + SOMEONE_ELSE)).status());
            assertEquals(403, call(mvc, post("/initial-amount").contentType(JSON).content(theirs)).status());
            assertEquals(403, call(mvc, put("/initial-amount/9").contentType(JSON).content(theirs)).status());
            verify(items, never()).findByUserIdAndItemTypeId(any(), anyLong());
            verify(items, never()).create(any());
            verify(items, never()).update(anyLong(), any());

            signIn(USER, "ROLE_USER", "ROLE_ADMIN");
            when(items.findByUserIdAndItemTypeId(SOMEONE_ELSE, 3L)).thenReturn(List.of(ia(9, 250)));
            assertEquals(200, call(mvc, get("/initial-amount/" + SOMEONE_ELSE)).status());
        }

        @Test
        void anAmountIsRequired() {
            Result r = call(mvc, post("/initial-amount").contentType(JSON).content("{\"userId\":\"" + USER + "\"}"));
            assertEquals(400, r.status(), r.body());
            assertTrue(r.has("InitialAmount Amount is required"));
        }
    }
}
