package com.financialplanner.moduleapi.mappers;

import com.financialplanner.moduleapi.dtos.debt.DebtRequest;
import com.financialplanner.moduleapi.dtos.debt.DebtResponse;
import com.financialplanner.moduleapi.dtos.paydown.DebtPaydownResponse;
import com.financialplanner.moduledisplaybc.paydown.PaydownModels.DebtPaydown;
import com.financialplanner.moduledisplaybc.paydown.PaydownModels.EventKind;
import com.financialplanner.moduledisplaybc.paydown.PaydownModels.PaydownEvent;
import com.financialplanner.moduleitemsbc.domain.debt.DebtDraft;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DebtMapperTest {

    private final DebtMapper mapper = new DebtMapper();

    private static Item item(long id, String name, double amount, String period) {
        Item item = new Item();
        item.setId(id);
        item.setName(name);
        item.setAmount(amount);
        item.setTimePeriod(new TimePeriod(6L, period));
        return item;
    }

    @Test
    void requestBecomesADraftWithExactDecimals() {
        DebtRequest request = new DebtRequest(10L, 1500.10, 24.99, LocalDate.of(2026, 10, 17),
                                              Arrays.asList(new DebtRequest.DebtAllocationRequest(20L, 33.33), null));

        DebtDraft draft = mapper.toDraft(request);

        assertEquals(10L, draft.itemId());
        assertEquals(new BigDecimal("1500.1"), draft.balance());
        assertEquals(new BigDecimal("24.99"), draft.apr());
        assertEquals(LocalDate.of(2026, 10, 17), draft.balanceDate());
        assertEquals(new BigDecimal("33.33"), draft.allocations().get(0).percent());
        // A null row stays a row, so the rules can reject it with a clear message
        assertNull(draft.allocations().get(1).creditItemId());
    }

    @Test
    void missingValuesStayMissing() {
        DebtDraft draft = mapper.toDraft(new DebtRequest(null, null, Double.NaN, null, null));

        assertNull(draft.balance());
        assertNull(draft.apr());
        assertTrue(draft.allocations().isEmpty());
    }

    @Test
    void responseCarriesTheDebitAndSortedAllocations() {
        Item card = item(10, "Chase Visa", 200, "Monthly");
        Debt debt = new Debt();
        debt.setId(1L);
        debt.setItem(card);
        debt.setBalance(new BigDecimal("1000.00"));
        debt.setApr(new BigDecimal("12.500"));
        debt.setBalanceDate(LocalDate.of(2026, 10, 17));
        debt.getAllocations().add(allocation(debt, 7L, item(21, "Paycheck", 2000, "Every Two Weeks"), "25.00"));
        debt.getAllocations().add(allocation(debt, 8L, item(22, "Bonus", 333.33, "Annual"), "33.33"));

        DebtResponse response = mapper.toResponse(debt);

        assertEquals(10L, response.itemId());
        assertEquals("Chase Visa", response.name());
        assertEquals(200.0, response.payment());
        assertEquals("Monthly", response.period());
        assertEquals(1000.0, response.balance());
        assertEquals(12.5, response.apr());
        assertEquals(List.of("Bonus", "Paycheck"),
                     response.allocations().stream().map(DebtResponse.DebtAllocationResponse::creditName).toList());
        assertEquals(111.10, response.allocations().get(0).perOccurrence()); // 33.33% of 333.33
        assertEquals(500.0, response.allocations().get(1).perOccurrence());
    }

    @Test
    void paydownBecomesPlainNumbersAndLowercaseKinds() {
        LocalDate oct30 = LocalDate.of(2026, 10, 30);
        DebtPaydown paydown = new DebtPaydown(1L, 10L, "Chase Visa", BigDecimal.ZERO, new BigDecimal("1000"),
                                              LocalDate.of(2026, 10, 17), new BigDecimal("1000"),
                                              new BigDecimal("500"), List.of(new BigDecimal("1000"), new BigDecimal("500")),
                                              new BigDecimal("500"), BigDecimal.ZERO, null,
                                              List.of(new PaydownEvent(oct30, EventKind.ALLOCATION, 20L, "Paycheck",
                                                                       new BigDecimal("25"), new BigDecimal("-500"),
                                                                       new BigDecimal("500"))));

        DebtPaydownResponse response = mapper.toPaydownResponse(List.of(paydown)).getFirst();

        assertEquals(List.of(1000.0, 500.0), response.balances());
        assertNull(response.payoffDate());
        assertEquals("allocation", response.events().getFirst().kind());
        assertEquals(-500.0, response.events().getFirst().amount());
        assertEquals(25.0, response.events().getFirst().percent());
    }

    private static DebtAllocation allocation(Debt debt, Long id, Item credit, String percent) {
        DebtAllocation allocation = new DebtAllocation();
        allocation.setId(id);
        allocation.setDebt(debt);
        allocation.setCreditItem(credit);
        allocation.setPercent(new BigDecimal(percent));
        return allocation;
    }
}
