package com.financialplanner.moduleapi.controllers;

import com.financialplanner.moduleapi.mappers.DebtMapper;
import com.financialplanner.moduleapi.support.ApiTestSupport.Result;
import com.financialplanner.moduleauth.domain.service.UserRolesService;
import com.financialplanner.moduledisplaybc.model.LedgerDto;
import com.financialplanner.moduledisplaybc.model.LedgerRequest;
import com.financialplanner.moduledisplaybc.service.LedgerReadoutService;
import com.financialplanner.moduleitemsbc.domain.service.DebtService;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Debt;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import com.financialplanner.modulecommonbc.exception.ItemNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.financialplanner.moduleapi.support.ApiTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

class SmallControllersTest {

    private static final UUID ME = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @AfterEach
    void clear() {
        signOut();
    }

    @Nested
    class UserRoles {
        private final UserRolesService service = mock(UserRolesService.class);
        private final MockMvc mvc = mvc(new UserRolesController(service, responses()));

        @Test
        void listsAddsRemovesAndReplacesRoles() {
            when(service.getRoleNamesForUser(7L)).thenReturn(List.of("ROLE_USER"));
            when(service.removeRoleFromUser(7L, 1L)).thenReturn(1);
            when(service.removeRoleFromUser(7L, 2L)).thenReturn(0);

            assertTrue(call(mvc, get("/users/7/roles")).has("ROLE_USER"));
            assertTrue(call(mvc, post("/users/7/roles/1")).has("Role added successfully"));
            verify(service).addRoleToUser(7L, 1L);
            assertTrue(call(mvc, delete("/users/7/roles/1")).has("Role removed successfully"));
            assertTrue(call(mvc, delete("/users/7/roles/2")).has("Role was not assigned to user"));
            assertTrue(call(mvc, put("/users/7/roles").contentType(JSON).content("{\"roleIds\":[1,2]}")).has("Roles replaced successfully"));
            verify(service).replaceRolesForUser(7L, List.of(1L, 2L));
        }
    }

    @Nested
    class ClientLogs {
        private final MockMvc mvc = mvc(new ClientLogController());

        @Test
        void acceptsErrorWarnAndInfoLogs() {
            for (String level : List.of("error", "warn", "info", "DEBUG")) {
                Result r = call(mvc, post("/client-logs").contentType(JSON)
                                                         .content("{\"level\":\"" + level + "\",\"message\":\"m\",\"url\":\"/x\",\"status\":500,\"correlationId\":\"c\"}"));
                assertEquals(200, r.status(), level);
            }
        }
    }

    @Nested
    class Display {
        private final LedgerReadoutService ledger = mock(LedgerReadoutService.class);
        private final MockMvc mvc = mvc(new DisplayController(ledger, responses(), new DebtMapper()));

        @Test
        void buildsTheLedgerForTheRequestedRange() {
            signIn(ME, "ROLE_USER");
            LedgerDto day = new LedgerDto();
            day.setWDate(LocalDate.of(2026, 10, 30));
            day.setRunningTotal(1600);
            when(ledger.buildLedgerReadout(any())).thenReturn(new ArrayList<>(List.of(day)));

            Result r = call(mvc, post("/display/ledger").contentType(JSON).content(
                "{\"userId\":\"" + ME + "\",\"ledgerStartDate\":\"2026-10-01\",\"ledgerEndDate\":\"2026-10-31\",\"includeGrouping\":false}"));

            assertEquals(200, r.status(), r.body());
            assertTrue(r.has("1600"));
            ArgumentCaptor<LedgerRequest> req = ArgumentCaptor.forClass(LedgerRequest.class);
            verify(ledger).buildLedgerReadout(req.capture());
            assertEquals(LocalDate.of(2026, 10, 1), req.getValue().ledgerStartDate());
            assertEquals(ME, req.getValue().userId());
        }

        @Test
        void theLedgerIsTheCallersOwnUnlessAdmin() {
            when(ledger.buildLedgerReadout(any())).thenReturn(new ArrayList<>());
            String other = "{\"userId\":\"" + UUID.randomUUID() + "\",\"ledgerStartDate\":\"2026-10-01\",\"ledgerEndDate\":\"2026-10-31\",\"includeGrouping\":false}";
            String mine = "{\"ledgerStartDate\":\"2026-10-01\",\"ledgerEndDate\":\"2026-10-31\",\"includeGrouping\":false}";

            // Signed out
            assertEquals(401, call(mvc, post("/display/ledger").contentType(JSON).content(mine)).status());

            signIn(ME, "ROLE_USER");
            Result r = call(mvc, post("/display/ledger").contentType(JSON).content(other));
            assertEquals(403, r.status());
            assertTrue(r.has("You can only see your own forecast"));
            verify(ledger, never()).buildLedgerReadout(any());

            // No userId means the caller
            assertEquals(200, call(mvc, post("/display/ledger").contentType(JSON).content(mine)).status());
            ArgumentCaptor<LedgerRequest> req = ArgumentCaptor.forClass(LedgerRequest.class);
            verify(ledger).buildLedgerReadout(req.capture());
            assertEquals(ME, req.getValue().userId());

            signIn(ME, "ROLE_USER", "ROLE_ADMIN");
            assertEquals(200, call(mvc, post("/display/ledger").contentType(JSON).content(other)).status());
        }
    }

    @Nested
    class Debts {
        private final DebtService service = mock(DebtService.class);
        private final MockMvc mvc = mvc(new DebtController(service, new DebtMapper(), responses()));

        private Debt debt() {
            Item card = new Item();
            card.setId(10L);
            card.setName("Chase Visa");
            card.setAmount(200.0);
            Debt d = new Debt();
            d.setId(1L);
            d.setUserId(ME);
            d.setItem(card);
            d.setBalance(new BigDecimal("1000.00"));
            d.setApr(BigDecimal.ZERO);
            d.setBalanceDate(LocalDate.of(2026, 10, 17));
            return d;
        }

        @Test
        void usesTheSignedInUserNotTheBody() {
            signIn(ME, "ROLE_USER");
            when(service.list(ME)).thenReturn(List.of(debt()));
            when(service.create(any(), eq(ME))).thenReturn(debt());

            Result list = call(mvc, get("/debts"));
            assertEquals(200, list.status());
            assertTrue(list.has("Chase Visa"));

            Result created = call(mvc, post("/debts").contentType(JSON).content("{\"itemId\":10,\"balance\":1000}"));
            assertEquals(201, created.status(), created.body());
            assertEquals("/debts/1", created.location());
        }

        @Test
        void passesTheAdminFlagForGetUpdateAndDelete() {
            signIn(ME, "ROLE_USER", "ROLE_ADMIN");
            when(service.get(1L, ME, true)).thenReturn(debt());
            when(service.update(eq(1L), any(), eq(ME), eq(true))).thenReturn(debt());

            assertEquals(200, call(mvc, get("/debts/1")).status());
            assertEquals(200, call(mvc, put("/debts/1").contentType(JSON).content("{\"balance\":900}")).status());
            assertTrue(call(mvc, delete("/debts/1")).has("Debt 1 deleted successfully"));
            verify(service).delete(1L, ME, true);
        }

        @Test
        void someoneElsesDebtIs404() {
            signIn(ME, "ROLE_USER");
            when(service.get(2L, ME, false)).thenThrow(new ItemNotFoundException("Debt 2 not found"));
            assertEquals(404, call(mvc, get("/debts/2")).status());
        }

        @Test
        void signedOutIs401() {
            assertEquals(401, call(mvc, get("/debts")).status());
        }
    }
}
