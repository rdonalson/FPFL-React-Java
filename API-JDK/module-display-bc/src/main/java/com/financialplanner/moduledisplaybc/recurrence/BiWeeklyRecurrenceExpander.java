package com.financialplanner.moduledisplaybc.recurrence;

import com.financialplanner.moduledisplaybc.model.ItemDto;
import com.financialplanner.moduledisplaybc.utility.RecurrenceRange;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Expands bi-weekly recurring items into individual detailed occurrences for a specified ledger date range.
 * The class processes a collection of items that may represent recurring schedules or events
 * and expands only those marked with a bi-weekly recurrence pattern. This is achieved by computing
 * the occurrence dates falling within the effective date range of the items and mapping them into
 * detailed representations (`ItemDto` objects).
 * Bi-weekly recurrence is identified using a specific time period ID (4), and the expansion considers
 * the effective date ranges, target day of week, and ledger boundaries. The resulting expanded list
 * provides detailed entries for each occurrence of the recurring items.
 * Key functionalities of this class include:
 * - Filtering items for bi-weekly recurrence.
 * - Calculating bi-weekly dates within a given range matching a specific day of the week.
 * - Handling dynamic effective ranges based on ledger boundaries and optional item configurations.
 */
public class BiWeeklyRecurrenceExpander {

    private final Function<Item, ItemDto> mapper;

    public BiWeeklyRecurrenceExpander(Function<Item, ItemDto> mapper) {
        this.mapper = mapper;
    }

    /**
     * Expands a list of items into a detailed list of `ItemDto` objects by generating bi-weekly
     * occurrences within a specified date range. Only items marked as bi-weekly are processed,
     * and their occurrences are determined based on their effective date range and target weekday.
     *
     * @param items      the list of items to be expanded, where each item may represent
     *                   a recurring schedule or event
     * @param ledgerStart the start date of the ledger range, which serves as the lower bound
     *                    for the effective range of item occurrences
     * @param ledgerEnd   the end date of the ledger range, which serves as the upper bound
     *                    for the effective range of item occurrences
     * @return a list of `ItemDto` objects, each representing a single occurrence of a bi-weekly item
     *         on a specific date within the specified ledger range
     */
    public List<ItemDto> expand(List<Item> items, LocalDate ledgerStart, LocalDate ledgerEnd) {
        List<ItemDto> expanded = new ArrayList<>();

        // Filter bi-weekly items first (periodId = 4)
        List<Item> biWeeklyItems = items.stream()
            .filter(this::isBiWeekly)
            .toList();

        for (Item item : biWeeklyItems) {

            LocalDate begin = item.getBeginDate();
            // Default effective range = ledger range
            LocalDate effStart = begin.isAfter(ledgerStart) ? begin : ledgerStart;
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

            // Determine target weekday (everyOtherWeekDow)
            Integer dowValue = item.getEveryOtherWeekDow();
            if (dowValue == null) {
                // Missing weekday → skip expansion
                continue;
            }

            DayOfWeek targetDow = DayOfWeek.of(dowValue);

            // Compute bi-weekly dates using the effective range
            List<LocalDate> biWeeklyDates = computeBiWeeklyDates(effStart, effEnd, targetDow);

            for (LocalDate date : biWeeklyDates) {
                ItemDto dto = mapper.apply(item);
                dto.setOccurrenceDate(date.toString());
                expanded.add(dto);
            }
        }

        return expanded;
    }

    /**
     * Checks if the given {@code Item} is associated with a bi-weekly recurrence pattern.
     * A bi-weekly item is identified by having a time period with an ID of 4.
     * If the {@code Item} or its associated time period is {@code null}, this method
     * will return {@code false}.
     *
     * @param item The {@code Item} to be checked for bi-weekly recurrence.
     *             May be {@code null}.
     * @return {@code true} if the {@code Item} is bi-weekly (time period ID equals 4),
     *         {@code false} otherwise.
     */
    public boolean isBiWeekly(Item item) {
        if (item == null || item.getTimePeriod() == null) return false;
        int pid = Math.toIntExact(item.getTimePeriod().getId());
        return pid == 4; // bi-weekly = 4
    }

    /**
     * Computes a list of bi-weekly dates within a specified range that match a given day of the week.
     * The method determines all dates falling on the specified {@code targetDay} within the
     * inclusive range defined by {@code start} and {@code end}, occurring at bi-weekly intervals.
     *
     * @param start the start date of the range; must not be null.
     *              The computation begins from this date to find the first occurrence of the {@code targetDay}.
     * @param end the end date of the range; must not be null and must not be before the {@code start} date.
     *            No dates beyond this date will be included in the result.
     * @param targetDay the day of the week to match (e.g., MONDAY, TUESDAY); must not be null.
     * @return a list of {@code LocalDate} objects representing bi-weekly occurrences of the {@code targetDay}
     *         within the specified range. If no matching dates are found, the returned list will be empty.
     */
    private static List<LocalDate> computeBiWeeklyDates(LocalDate start, LocalDate end, DayOfWeek targetDay) {
        List<LocalDate> dates = new ArrayList<>();

        LocalDate cursor = start;

        // Snap forward to first matching weekday
        while (cursor.getDayOfWeek() != targetDay) {
            cursor = cursor.plusDays(1);
            if (cursor.isAfter(end)) {
                return dates; // no occurrences in range
            }
        }

        // Add bi-weekly occurrences
        while (!cursor.isAfter(end)) {
            dates.add(cursor);
            cursor = cursor.plusWeeks(2);
        }

        return dates;
    }
}
