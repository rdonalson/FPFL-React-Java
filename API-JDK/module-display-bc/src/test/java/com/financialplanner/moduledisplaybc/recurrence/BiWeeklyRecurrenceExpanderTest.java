package com.financialplanner.moduledisplaybc.recurrence;

import com.financialplanner.moduledisplaybc.model.ItemDto;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.TimePeriod;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Every-two-weeks items keep their rhythm from the begin date, whatever range the ledger asks for. */
class BiWeeklyRecurrenceExpanderTest {

    private static LocalDate d(int month, int day) {
        return LocalDate.of(2026, month, day);
    }

    private final BiWeeklyRecurrenceExpander expander = new BiWeeklyRecurrenceExpander(i -> new ItemDto());

    private static Item paycheck(LocalDate begin) {
        Item item = new Item();
        item.setId(20L);
        item.setName("Paycheck");
        item.setAmount(2000.0);
        item.setTimePeriod(new TimePeriod(4L, "Every Two Weeks"));
        item.setEveryOtherWeekDow(5); // Friday
        item.setBeginDate(begin);
        return item;
    }

    private List<LocalDate> dates(Item item, LocalDate start, LocalDate end) {
        return expander.expand(List.of(item), start, end).stream().map(dto -> LocalDate.parse(dto.getOccurrenceDate())).toList();
    }

    @Test
    void theRhythmDoesNotShiftWithTheLedgerStart() {
        Item item = paycheck(d(10, 2)); // a Friday: Oct 2, 16, 30, Nov 13 …

        assertEquals(List.of(d(10, 30), d(11, 13), d(11, 27)), dates(item, d(10, 18), d(11, 30)));
        assertEquals(List.of(d(10, 30), d(11, 13), d(11, 27)), dates(item, d(10, 24), d(11, 30)));
        assertEquals(List.of(d(10, 16), d(10, 30)), dates(item, d(10, 10), d(10, 31)));
    }

    @Test
    void nothingBeforeTheBeginDate() {
        Item item = paycheck(d(10, 14)); // a Wednesday: first Friday is Oct 16

        assertEquals(List.of(d(10, 16), d(10, 30)), dates(item, d(9, 1), d(10, 31)));
    }

    @Test
    void aMissingBeginDateCountsFromTheLedgerStart() {
        Item item = paycheck(null);

        assertEquals(List.of(d(10, 23), d(11, 6)), dates(item, d(10, 18), d(11, 6)));
    }

    @Test
    void computesAcrossLongGapsWithoutDrift() {
        List<LocalDate> dates = BiWeeklyRecurrenceExpander.computeBiWeeklyDates(
            LocalDate.of(2020, 1, 3), d(10, 1), d(10, 31), DayOfWeek.FRIDAY);

        assertFalse(dates.isEmpty());
        dates.forEach(date -> assertEquals(0, java.time.temporal.ChronoUnit.DAYS.between(LocalDate.of(2020, 1, 3), date) % 14));
        assertTrue(BiWeeklyRecurrenceExpander.computeBiWeeklyDates(d(10, 2), d(10, 31), d(10, 1), DayOfWeek.FRIDAY).isEmpty());
    }
}
