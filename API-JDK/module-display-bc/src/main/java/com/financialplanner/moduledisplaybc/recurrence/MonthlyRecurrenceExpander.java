package com.financialplanner.moduledisplaybc.recurrence;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import com.financialplanner.moduledisplaybc.model.ItemDto;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * The {@code MonthlyRecurrenceExpander} class is responsible for expanding a collection of items
 * into their monthly occurrences based on specific recurrence rules. It is designed to handle
 * items that follow a monthly recurrence pattern and generates occurrence dates within a specified
 * date range.
 */
public class MonthlyRecurrenceExpander {

    private final Function<Item, ItemDto> mapper;

    public MonthlyRecurrenceExpander(Function<Item, ItemDto> mapper) {
        this.mapper = mapper;
    }

    /**
     * Monthly recurrence (periodId = 6)
     * Uses MonthlyDom (day-of-month) to generate one occurrence per month.
     */
    public List<ItemDto> expand(List<Item> items, LocalDate ledgerStart, LocalDate ledgerEnd) {
        List<ItemDto> expanded = new ArrayList<>();

        for (Item item : items) {

            int periodId = item.getTimePeriod() != null ? Math.toIntExact(item.getTimePeriod().getId()) : 0;

            // Only handle monthly (periodId = 6)
            if (periodId != 6) {
                continue;
            }

            Integer dom = item.getMonthlyDom();
            if (dom == null) {
                // No day-of-month → treat as single occurrence
                expanded.add(mapper.apply(item));
                continue;
            }

            // Expand month-by-month
            YearMonth cursor = YearMonth.from(ledgerStart);

            while (!cursor.atEndOfMonth().isBefore(ledgerStart) &&
                !cursor.atDay(1).isAfter(ledgerEnd)) {

                LocalDate occurrence = safeDayOfMonth(cursor, dom);

                if (!occurrence.isBefore(ledgerStart) && !occurrence.isAfter(ledgerEnd)) {
                    ItemDto dto = mapper.apply(item);
                    dto.setOccurrenceDate(occurrence.toString());
                    expanded.add(dto);
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
     * Determines if the given item has a time period associated with monthly recurrence.
     *
     * @param item The item to check for monthly recurrence. It must have a valid time period.
     * @return {@code true} if the item's time period ID corresponds to monthly recurrence (ID = 6),
     *         {@code false} otherwise.
     */
    public boolean isMonthly(Item item) {
        if (item == null || item.getTimePeriod() == null) return false;
        int pid = Math.toIntExact(item.getTimePeriod().getId());
        return pid == 6;
    }
}
