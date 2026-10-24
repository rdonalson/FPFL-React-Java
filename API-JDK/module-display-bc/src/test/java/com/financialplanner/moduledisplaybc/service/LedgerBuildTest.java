package com.financialplanner.moduledisplaybc.service;

import com.financialplanner.moduledisplaybc.model.ItemDto;
import com.financialplanner.moduledisplaybc.model.LedgerDto;
import com.financialplanner.moduledisplaybc.model.LedgerRequest;
import com.financialplanner.moduleitemsbc.domain.service.DebtService;
import com.financialplanner.moduleitemsbc.domain.service.ItemService;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** The forecast ledger itself: one row per day, the initial amount, credits, debits and the running total. */
class LedgerBuildTest {

    private static final UUID USER = UUID.randomUUID();

    private static Item item(long id, String name, long type, double amount, long period) {
        Item item = new Item();
        item.setId(id);
        item.setUserId(USER);
        item.setName(name);
        item.setAmount(amount);
        item.setItemType(new ItemType(type, type == 1 ? "Credit" : type == 2 ? "Debit" : "Initial Amount"));
        if (period > 0) item.setTimePeriod(new TimePeriod(period, "Monthly"));
        return item;
    }

    private List<LedgerDto> ledger(LocalDate start, LocalDate end, Item... items) {
        ItemService itemService = mock(ItemService.class);
        DebtService debtService = mock(DebtService.class);
        when(itemService.findByUserId(USER)).thenReturn(List.of(items));
        when(debtService.list(USER)).thenReturn(List.of());
        return new LedgerReadoutServiceImpl(itemService, debtService)
            .buildLedgerReadout(new LedgerRequest(USER, start, end, false));
    }

    @Test
    void oneRowPerDayWithSequentialKeys() {
        List<LedgerDto> rows = ledger(LocalDate.of(2026, 1, 30), LocalDate.of(2026, 2, 2));
        assertEquals(4, rows.size());
        assertEquals(List.of(1, 2, 3, 4), rows.stream().map(LedgerDto::getRollupKey).toList());
        assertEquals(LocalDate.of(2026, 2, 2), rows.getLast().getWDate());
        assertEquals(2026, rows.getFirst().getYear());
    }

    @Test
    void theInitialAmountStartsTheRunningTotalAndIsNotAnEntry() {
        List<LedgerDto> rows = ledger(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 3), item(1, "Initial Amount", 3, 500, 0));

        rows.forEach(r -> assertEquals(500.0, r.getRunningTotal()));
        assertTrue(rows.stream().allMatch(r -> r.getItems().isEmpty()));
    }

    @Test
    void creditsAddDebitsSubtractAndTheTotalRollsForward() {
        Item pay = item(2, "Paycheck", 1, 2000, 6);
        pay.setMonthlyDom(1);
        Item rent = item(3, "Rent", 2, 1600, 6);
        rent.setMonthlyDom(2);

        List<LedgerDto> rows = ledger(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 2), item(1, "IA", 3, 100, 0), pay, rent);

        LedgerDto jan1 = rows.get(0);
        assertEquals(2000.0, jan1.getCreditSummary());
        assertEquals(0.0, jan1.getDebitSummary());
        assertEquals(2100.0, jan1.getRunningTotal());

        LedgerDto jan2 = rows.get(1);
        assertEquals(-1600.0, jan2.getDebitSummary());
        assertEquals(-1600.0, jan2.getNet());
        assertEquals(500.0, jan2.getRunningTotal());

        ItemDto entry = jan2.getItems().getFirst();
        assertEquals("Rent", entry.getName());
        assertEquals(-1600.0, entry.getAmount());
        assertEquals("Debit", entry.getItemType());
        assertEquals("Monthly", entry.getPeriod());
        assertEquals(1, entry.getItemKey());

        assertEquals(100 + 2 * 2000 - 2 * 1600, rows.getLast().getRunningTotal(), 0.001);
    }

    @Test
    void aSundayWeeklyItemNoLongerBreaksTheLedger() {
        Item church = item(4, "Offering", 2, 20, 3);
        church.setTimePeriod(new TimePeriod(3L, "Weekly"));
        church.setWeeklyDow(0); // Sunday, as the UI saves it

        List<LedgerDto> rows = assertDoesNotThrow(() -> ledger(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), church));

        assertEquals(-80.0, rows.stream().mapToDouble(LedgerDto::getDebitSummary).sum());
    }
}
