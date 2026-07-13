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
 * The {@code MonthlyRecurrenceExpander} class is responsible for expanding a list of items
 * into their corresponding monthly recurring representations, taking into account
 * ledger date ranges, day-of-month values, and other recurrence rules. This utility is
 * used in scenarios where monthly occurrences of financial or event-related data need
 * to be computed and represented.
 */
public class MonthlyRecurrenceExpander {

    private final Function<Item, ItemDto> mapper;

    public MonthlyRecurrenceExpander(Function<Item, ItemDto> mapper) {
        this.mapper = mapper;
    }

    /**
     * Expands a list of {@code Item} objects into a list of {@code ItemDto} instances,
     * generating monthly occurrences within a specified ledger date range. The method
     * filters items that are marked as monthly recurring and calculates occurrences
     * based on specified day-of-month values.
     *
     * @param items the list of items to expand; items not fitting the monthly recurrence criteria are excluded
     * @param ledgerStart the start date of the ledger range to consider for calculating monthly occurrences
     * @param ledgerEnd the end date of the ledger range to consider for calculating monthly occurrences
     * @return a list of {@code ItemDto} objects where each represents a monthly occurrence of an input item
     */
    public List<ItemDto> expand(List<Item> items, LocalDate ledgerStart, LocalDate ledgerEnd) {
        List<ItemDto> expanded = new ArrayList<>();

        // Filter monthly items first (periodId = 6)
        List<Item> monthlyItems = items.stream()
            .filter(this::isMonthly)
            .toList();

        for (Item item : monthlyItems) {

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

            Integer dom = item.getMonthlyDom();
            if (dom == null) {
                // Missing day-of-month → skip expansion
                continue;
            }

            // Compute monthly dates using the effective range
            List<LocalDate> monthlyDates = computeMonthlyDates(effStart, effEnd, dom);

            for (LocalDate date : monthlyDates) {
                ItemDto dto = mapper.apply(item);
                dto.setOccurrenceDate(date.toString());
                expanded.add(dto);
            }
        }

        return expanded;
    }

    /**
     * Determines if the given item is classified as a "monthly" item based on its time period.
     *
     * @param item the item to check, which may contain a time period associated with it
     * @return true if the item's time period indicates it is monthly (period ID equals 6);
     *         false otherwise, including cases where the item or its time period is null
     */
    public boolean isMonthly(Item item) {
        if (item == null || item.getTimePeriod() == null) return false;
        int pid = Math.toIntExact(item.getTimePeriod().getId());
        return pid == 6; // monthly = 6
    }

    /**
     * Computes a list of monthly dates within the specified range based on a given day-of-month (dom).
     * If the specified day-of-month exceeds the total number of days in a month, the last day
     * of that month is used instead.
     *
     * @param start the start date of the range; must not be null.
     * @param end the end date of the range; must not be null and must not be before the start date.
     * @param dom the day of the month on which occurrences should be generated. If the day exceeds
     *            the number of days in a given month, the last valid day of that month is used.
     * @return a list of {@code LocalDate} objects representing the computed monthly dates within
     *         the specified range.
     */
    private static List<LocalDate> computeMonthlyDates(LocalDate start, LocalDate end, int dom) {
        List<LocalDate> dates = new ArrayList<>();

        YearMonth cursor = YearMonth.from(start);

        while (!cursor.atEndOfMonth().isBefore(start) &&
            !cursor.atDay(1).isAfter(end)) {

            LocalDate occurrence = safeDayOfMonth(cursor, dom);

            if (!occurrence.isBefore(start) && !occurrence.isAfter(end)) {
                dates.add(occurrence);
            }

            cursor = cursor.plusMonths(1);
        }

        return dates;
    }

    /**
     * Computes a safe day of the month for a given {@code YearMonth} and day.
     * If the specified day exceeds the maximum number of days in the month,
     * the last day of the month is returned instead.
     *
     * @param ym the {@code YearMonth} representing the target year and month
     * @param day the desired day of the month
     * @return a {@code LocalDate} representing the resolved date, guaranteed to be within the bounds of the month
     */
    private static LocalDate safeDayOfMonth(YearMonth ym, int day) {
        int lastDay = ym.lengthOfMonth();
        int safeDay = Math.min(day, lastDay);
        return ym.atDay(safeDay);
    }
}
