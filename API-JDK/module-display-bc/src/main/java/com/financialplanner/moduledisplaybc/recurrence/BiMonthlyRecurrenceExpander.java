package com.financialplanner.moduledisplaybc.recurrence;

import com.financialplanner.moduledisplaybc.model.ItemDto;
import com.financialplanner.moduledisplaybc.utility.RecurrenceRange;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * The {@code BiMonthlyRecurrenceExpander} class provides functionality to expand bi-monthly
 * recurring items into individual occurrences within a specified date range.
 * It leverages a provided mapping function to convert items into their DTO representations.
 */
public class BiMonthlyRecurrenceExpander {

    private final Function<Item, ItemDto> mapper;

    public BiMonthlyRecurrenceExpander(Function<Item, ItemDto> mapper) {
        this.mapper = mapper;
    }

    /**
     * Expands a list of bi-monthly recurring items into individual occurrences based on the provided ledger date range.
     * This method filters items marked as bi-monthly and computes their specific occurrence dates within the effective range.
     * The expanded occurrences are returned as a list of {@code ItemDto}.
     *
     * @param items       the list of items to evaluate and expand
     * @param ledgerStart the start date of the ledger range
     * @param ledgerEnd   the end date of the ledger range
     * @return a list of {@code ItemDto} objects representing the expanded occurrences of bi-monthly items
     */
    public List<ItemDto> expand(List<Item> items, LocalDate ledgerStart, LocalDate ledgerEnd) {
        List<ItemDto> expanded = new ArrayList<>();

        // Filter bi-monthly items first (periodId = 5)
        List<Item> biMonthlyItems = items.stream()
            .filter(this::isBiMonthly)
            .toList();

        for (Item item : biMonthlyItems) {

            // Default effective range = ledger range
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

            Integer day1 = item.getBiMonthlyDay1();
            Integer day2 = item.getBiMonthlyDay2();

            if (day1 == null && day2 == null) {
                // Missing recurrence days → skip expansion
                continue;
            }

            // Compute bi-monthly dates using the effective range
            List<LocalDate> biMonthlyDates = computeBiMonthlyDates(effStart, effEnd, day1, day2);

            for (LocalDate date : biMonthlyDates) {
                ItemDto dto = mapper.apply(item);
                dto.setOccurrenceDate(date.toString());
                expanded.add(dto);
            }
        }

        return expanded;
    }

    /**
     * Checks if the given item is bi-monthly based on its time period identifier.
     *
     * @param item the item to check; may be null.
     * @return true if the item's time period identifier corresponds to bi-monthly (5);
     *         false otherwise.
     */
    public boolean isBiMonthly(Item item) {
        if (item == null || item.getTimePeriod() == null) return false;
        int pid = Math.toIntExact(item.getTimePeriod().getId());
        return pid == 5; // bi-monthly = 5
    }

    /**
     * Computes a list of bi-monthly dates within a specified date range,
     * based on two specified days of the month. The method ensures that
     * the dates are within the specified start and end boundaries.
     *
     * @param start the start date of the range; must not be null.
     * @param end the end date of the range; must not be null and must not be before the start date.
     * @param day1 the first day of the month to include in the computed dates; may be null to ignore this day.
     * @param day2 the second day of the month to include in the computed dates; may be null to ignore this day.
     * @return a list of {@code LocalDate} objects representing the computed bi-monthly dates within the specified range.
     */
    private static List<LocalDate> computeBiMonthlyDates(LocalDate start, LocalDate end,
                                                         Integer day1, Integer day2) {

        List<LocalDate> dates = new ArrayList<>();

        YearMonth cursor = YearMonth.from(start);

        while (!cursor.atEndOfMonth().isBefore(start) &&
            !cursor.atDay(1).isAfter(end)) {

            // First day
            if (day1 != null) {
                LocalDate d1 = safeDayOfMonth(cursor, day1);
                if (!d1.isBefore(start) && !d1.isAfter(end)) {
                    dates.add(d1);
                }
            }

            // Second day
            if (day2 != null) {
                LocalDate d2 = safeDayOfMonth(cursor, day2);
                if (!d2.isBefore(start) && !d2.isAfter(end)) {
                    dates.add(d2);
                }
            }

            cursor = cursor.plusMonths(1);
        }

        return dates;
    }

    /**
     * Ensures that the specified day of the month is within the valid range of days
     * for the given {@code YearMonth}. If the provided day exceeds the last valid day
     * of the {@code YearMonth}, it is adjusted to the last day of the month.
     *
     * @param ym the {@code YearMonth} for which the day is being validated; must not be null.
     * @param day the desired day of the month; can be any integer.
     * @return a {@code LocalDate} representing either the specified day of the month
     *         or the last day of the {@code YearMonth}, whichever is earlier.
     */
    private static LocalDate safeDayOfMonth(YearMonth ym, int day) {
        int lastDay = ym.lengthOfMonth();
        int safeDay = Math.min(day, lastDay);
        return ym.atDay(safeDay);
    }
}
