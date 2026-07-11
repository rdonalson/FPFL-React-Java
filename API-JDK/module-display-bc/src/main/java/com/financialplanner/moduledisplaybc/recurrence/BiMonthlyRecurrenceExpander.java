package com.financialplanner.moduledisplaybc.recurrence;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import com.financialplanner.moduledisplaybc.model.ItemDto;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Expands bi-monthly recurrence rules into specific dates within a defined date range.
 * This class processes items that have a bi-monthly recurrence pattern and generates
 * one or two occurrences per month based on defined day offsets.
 */
public class BiMonthlyRecurrenceExpander {

    private final Function<Item, ItemDto> mapper;

    public BiMonthlyRecurrenceExpander(Function<Item, ItemDto> mapper) {
        this.mapper = mapper;
    }

    /**
     * Bi-monthly recurrence (periodId = 5)
     * Uses BiMonthlyDay1 and BiMonthlyDay2 to generate two occurrences per month.
     */
    public List<ItemDto> expand(List<Item> items, LocalDate ledgerStart, LocalDate ledgerEnd) {
        List<ItemDto> expanded = new ArrayList<>();

        for (Item item : items) {

            int periodId = Math.toIntExact(item.getTimePeriod() != null ? item.getTimePeriod().getId() : 0);

            // Only handle bi-monthly (periodId = 5)
            if (periodId != 5) {
                continue;
            }

            Integer day1 = item.getBiMonthlyDay1();
            Integer day2 = item.getBiMonthlyDay2();

            if (day1 == null && day2 == null) {
                // No days defined → treat as single occurrence
                expanded.add(mapper.apply(item));
                continue;
            }

            // Expand month-by-month
            YearMonth cursor = YearMonth.from(ledgerStart);

            while (!cursor.atEndOfMonth().isBefore(ledgerStart) &&
                !cursor.atDay(1).isAfter(ledgerEnd)) {

                // First day
                if (day1 != null) {
                    LocalDate d1 = safeDayOfMonth(cursor, day1);
                    if (!d1.isBefore(ledgerStart) && !d1.isAfter(ledgerEnd)) {
                        ItemDto dto = mapper.apply(item);
                        dto.setOccurrenceDate(d1.toString());
                        expanded.add(dto);
                    }
                }

                // Second day
                if (day2 != null) {
                    LocalDate d2 = safeDayOfMonth(cursor, day2);
                    if (!d2.isBefore(ledgerStart) && !d2.isAfter(ledgerEnd)) {
                        ItemDto dto = mapper.apply(item);
                        dto.setOccurrenceDate(d2.toString());
                        expanded.add(dto);
                    }
                }

                cursor = cursor.plusMonths(1);
            }
        }

        return expanded;
    }

    /**
     * Returns a valid LocalDate for the given day-of-month.
     * If the day exceeds the number of days in the month (e.g., 31 in February),
     * it snaps to the last day of the month.
     */
    private LocalDate safeDayOfMonth(YearMonth ym, int day) {
        int lastDay = ym.lengthOfMonth();
        int safeDay = Math.min(day, lastDay);
        return ym.atDay(safeDay);
    }

    /**
     * Determines whether a given item is bi-monthly based on its time period.
     *
     * @param item the item to be checked; may be null
     * @return true if the item's time period has an ID of 5 indicating bi-monthly recurrence,
     *         otherwise false
     */
    public boolean isBiMonthly(Item item) {
        if (item == null || item.getTimePeriod() == null) return false;
        int pid = Math.toIntExact(item.getTimePeriod().getId());
        return pid == 5;
    }
}
