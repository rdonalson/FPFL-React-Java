package com.financialplanner.moduledisplaybc.utility;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class RecurrenceRangeTest {

    private static final LocalDate START = LocalDate.of(2026, 1, 1);
    private static final LocalDate END = LocalDate.of(2026, 12, 31);

    private static Item item(Boolean req, LocalDate begin, LocalDate end) {
        Item item = new Item();
        item.setDateRangeReq(req);
        item.setBeginDate(begin);
        item.setEndDate(end);
        return item;
    }

    @Test
    void withoutATimeFrameTheLedgerRangeIsUsed() {
        assertArrayEquals(new LocalDate[] {START, END}, RecurrenceRange.resolveRange(item(false, null, null), START, END));
        assertArrayEquals(new LocalDate[] {START, END}, RecurrenceRange.resolveRange(item(null, null, null), START, END));
    }

    @Test
    void aTimeFrameIsIntersectedWithTheLedger() {
        LocalDate[] range = RecurrenceRange.resolveRange(item(true, LocalDate.of(2025, 6, 1), LocalDate.of(2026, 3, 31)), START, END);
        assertArrayEquals(new LocalDate[] {START, LocalDate.of(2026, 3, 31)}, range);
    }

    @Test
    void noOverlapMeansNoRange() {
        assertNull(RecurrenceRange.resolveRange(item(true, LocalDate.of(2027, 1, 1), LocalDate.of(2027, 2, 1)), START, END));
    }

    @Test
    void anIncompleteTimeFrameFallsBackToTheLedger() {
        assertArrayEquals(new LocalDate[] {START, END}, RecurrenceRange.resolveRange(item(true, null, LocalDate.of(2026, 3, 1)), START, END));
    }
}
