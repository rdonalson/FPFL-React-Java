package com.financialplanner.moduledisplaybc.recurrence;

import java.time.DayOfWeek;

/**
 * Reads a stored weekday. The UI saves 0 = Sunday … 6 = Saturday; the assistant drafts and ISO use
 * 1 = Monday … 7 = Sunday. Both are accepted (they only differ for Sunday). Anything else is no weekday, so
 * one bad item is skipped instead of failing the whole ledger.
 */
public final class Weekdays {

    private Weekdays() {}

    /** The weekday for {@code value}, or null when it isn't one. */
    public static DayOfWeek of(Integer value) {
        if (value == null || value < 0 || value > 7) {
            return null;
        }
        return DayOfWeek.of(value == 0 ? 7 : value);
    }
}
