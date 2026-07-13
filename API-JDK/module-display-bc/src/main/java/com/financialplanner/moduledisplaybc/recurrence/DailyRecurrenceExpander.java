package com.financialplanner.moduledisplaybc.recurrence;

import com.financialplanner.moduledisplaybc.model.ItemDto;
import com.financialplanner.moduledisplaybc.utility.RecurrenceRange;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * A utility class for expanding daily recurring {@code Item} objects into individual
 * occurrences represented as {@code ItemDto} objects over a specified date range.
 * The class processes items with daily recurrence, computes their occurrence dates
 * within a given range, and generates corresponding DTOs for each occurrence.
 */
public class DailyRecurrenceExpander {

    private final Function<Item, ItemDto> mapper;

    public DailyRecurrenceExpander(Function<Item, ItemDto> mapper) {
        this.mapper = mapper;
    }

    /**
     * Expands a list of daily recurring {@code Item} objects into a list of {@code ItemDto} objects,
     * generating individual occurrences for each day within the specified date range.
     *
     * @param items a list of {@code Item} objects to be expanded
     * @param ledgerStart the start date of the ledger range
     * @param ledgerEnd the end date of the ledger range
     * @return a list of {@code ItemDto} objects with occurrences for each applicable date
     */
    public List<ItemDto> expand(List<Item> items, LocalDate ledgerStart, LocalDate ledgerEnd) {
        List<ItemDto> expanded = new ArrayList<>();

        // Filter daily items first (periodId = 2)
        List<Item> dailyItems = items.stream()
            .filter(this::isDaily)
            .toList();

        for (Item item : dailyItems) {

            // Default range = ledger range
            LocalDate effStart = ledgerStart;
            LocalDate effEnd   = ledgerEnd;

            // Only call resolveRange() when DateRangeReq == true
            Boolean req = item.getDateRangeReq();
            if (req != null && req) {
                LocalDate[] range = RecurrenceRange.resolveRange(item, ledgerStart, ledgerEnd);

                if (range == null) {
                    // No overlap → skip item entirely
                    continue;
                }

                effStart = range[0];
                effEnd   = range[1];
            }

            // Expand daily occurrences using the effective range
            List<LocalDate> dates = computeDailyDates(effStart, effEnd);

            for (LocalDate date : dates) {
                ItemDto dto = mapper.apply(item);
                dto.setOccurrenceDate(date.toString());
                expanded.add(dto);
            }
        }

        return expanded;
    }

    /**
     * Determines if the given item has a daily recurrence pattern.
     *
     * An item is considered to have a daily recurrence pattern if its time period
     * is non-null and the associated period ID is equal to 2.
     *
     * @param item The item to be checked. May be null.
     * @return {@code true} if the item has a daily recurrence pattern, {@code false} otherwise.
     */
    public boolean isDaily(Item item) {
        if (item == null || item.getTimePeriod() == null) return false;
        int pid = Math.toIntExact(item.getTimePeriod().getId());
        return pid == 2;
    }

    /**
     * Computes a list of daily dates within a specified range, inclusive of the start and end dates.
     *
     * @param start the start date of the range; must not be null.
     * @param end the end date of the range; must not be null and must not be before the start date.
     * @return a list of {@code LocalDate} objects representing all dates from the start date to the end date, inclusive.
     */
    private static List<LocalDate> computeDailyDates(LocalDate start, LocalDate end) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate cursor = start;

        while (!cursor.isAfter(end)) {
            dates.add(cursor);
            cursor = cursor.plusDays(1);
        }

        return dates;
    }
}
