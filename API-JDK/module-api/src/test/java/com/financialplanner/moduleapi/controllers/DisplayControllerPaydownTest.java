package com.financialplanner.moduleapi.controllers;

import com.financialplanner.moduleapi.dtos.paydown.DebtPaydownResponse;
import com.financialplanner.moduleapi.mappers.DebtMapper;
import com.financialplanner.moduleapi.response.ApiResponse;
import com.financialplanner.moduleapi.response.ApiResponseFactory;
import com.financialplanner.moduleapi.security.CustomUserDetails;
import com.financialplanner.moduleauth.infrastructure.persistence.entity.User;
import com.financialplanner.modulecommonbc.exception.ForbiddenOperationException;
import com.financialplanner.modulecommonbc.exception.InvalidCredentialsException;
import com.financialplanner.modulecommonbc.sanitizer.SanitizerImpl;
import com.financialplanner.moduledisplaybc.model.ItemDto;
import com.financialplanner.moduledisplaybc.model.LedgerDto;
import com.financialplanner.moduledisplaybc.model.LedgerRequest;
import com.financialplanner.moduledisplaybc.paydown.PaydownModels.DebtPaydown;
import com.financialplanner.moduledisplaybc.paydown.PaydownModels.EventKind;
import com.financialplanner.moduledisplaybc.paydown.PaydownModels.PaydownEvent;
import com.financialplanner.moduledisplaybc.service.LedgerReadoutService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * POST /display/paydown: who may see whose paydown, and that the real sanitizer lets Debt Paydown's
 * "25% of Paycheck" text through (the strict default would reject the "%").
 */
class DisplayControllerPaydownTest {

    private static final UUID ME = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final LocalDate START = LocalDate.of(2026, 10, 18);
    private static final LocalDate END = LocalDate.of(2026, 11, 30);

    private final FakeLedger ledger = new FakeLedger();
    private final DisplayController controller =
        new DisplayController(ledger, new ApiResponseFactory(new SanitizerImpl()), new DebtMapper());

    private static CustomUserDetails principal(UUID userId, String... roles) {
        User user = new User();
        user.setEmail("rick@example.com");
        user.setUserId(userId);
        return new CustomUserDetails(user, List.of(roles));
    }

    @Test
    void defaultsToTheCallersOwnPaydown() {
        ApiResponse<List<DebtPaydownResponse>> body =
            controller.buildPaydown(new LedgerRequest(null, START, END, false), principal(ME, "ROLE_USER")).getBody();

        assertEquals(ME, ledger.lastRequest.userId());
        assertEquals(START, ledger.lastRequest.ledgerStartDate());
        assertNotNull(body);
        DebtPaydownResponse debt = body.getData().getFirst();
        assertEquals("Chase Visa", debt.name());
        assertEquals("allocation", debt.events().getFirst().kind());
        assertEquals("Paycheck", debt.events().getFirst().sourceName());
        assertEquals(LocalDate.of(2026, 11, 13), debt.payoffDate());
    }

    @Test
    void someoneElsesPaydownIsForbiddenUnlessAdmin() {
        assertThrows(ForbiddenOperationException.class,
                     () -> controller.buildPaydown(new LedgerRequest(OTHER, START, END, false), principal(ME, "ROLE_USER")));

        controller.buildPaydown(new LedgerRequest(OTHER, START, END, false), principal(ME, "ROLE_ADMIN"));
        assertEquals(OTHER, ledger.lastRequest.userId());
    }

    @Test
    void signedOutCallersAreRejected() {
        assertThrows(InvalidCredentialsException.class,
                     () -> controller.buildPaydown(new LedgerRequest(null, START, END, false), null));
    }

    @Test
    void theLedgersPaydownEntriesSurviveSanitizing() {
        ApiResponse<List<LedgerDto>> body = controller.buildLedger(new LedgerRequest(ME, START, END, false)).getBody();

        assertNotNull(body);
        ItemDto entry = body.getData().getFirst().getItems().getFirst();
        assertEquals("25% of Paycheck", entry.getPeriod());
        assertEquals("Chase Visa paydown", entry.getName());
    }

    private static final class FakeLedger implements LedgerReadoutService {
        LedgerRequest lastRequest;

        @Override
        public List<LedgerDto> buildLedgerReadout(LedgerRequest request) {
            ItemDto entry = new ItemDto();
            entry.setFkItemType(2);
            entry.setItemType(PAYDOWN_TYPE_NAME);
            entry.setName("Chase Visa paydown");
            entry.setPeriod("25% of Paycheck");
            entry.setAmount(-500.0);
            entry.setOccurrenceDate("2026-10-30");

            LedgerDto day = new LedgerDto();
            day.setWDate(LocalDate.of(2026, 10, 30));
            day.setItems(new ArrayList<>(List.of(entry)));
            return new ArrayList<>(List.of(day));
        }

        @Override
        public List<DebtPaydown> buildPaydown(LedgerRequest request) {
            lastRequest = request;
            PaydownEvent event = new PaydownEvent(LocalDate.of(2026, 10, 30), EventKind.ALLOCATION, 20L, "Paycheck",
                                                  new BigDecimal("25"), new BigDecimal("-500"), new BigDecimal("500"));
            return List.of(new DebtPaydown(1L, 10L, "Chase Visa", BigDecimal.ZERO, new BigDecimal("1000"),
                                           LocalDate.of(2026, 10, 17), new BigDecimal("1000"), new BigDecimal("500"),
                                           List.of(new BigDecimal("500")), new BigDecimal("500"), BigDecimal.ZERO,
                                           LocalDate.of(2026, 11, 13), List.of(event)));
        }
    }
}
