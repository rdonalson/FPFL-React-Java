package com.financialplanner.moduledisplaybc.recurrence;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class providing methods for handling recurrence-related computations.
 * This class is final and cannot be instantiated.
 */
public final class RecurrenceUtils {

    private RecurrenceUtils() { /* utility */ }

    /**
     * Return all dates between start and end (inclusive) that fall on targetDay.
     * Assumes start <= end.
     */
    public static List<LocalDate> computeWeeklyDates(LocalDate start, LocalDate end, DayOfWeek targetDay) {
        List<LocalDate> dates = new ArrayList<>();

        LocalDate cursor = start;
        // advance to first matching day (safe even if start already matches)
        while (cursor.getDayOfWeek() != targetDay) {
            cursor = cursor.plusDays(1);
            if (cursor.isAfter(end)) {
                return dates; // no occurrences in range
            }
        }

        while (!cursor.isAfter(end)) {
            dates.add(cursor);
            cursor = cursor.plusWeeks(1);
        }

        return dates;
    }
}
