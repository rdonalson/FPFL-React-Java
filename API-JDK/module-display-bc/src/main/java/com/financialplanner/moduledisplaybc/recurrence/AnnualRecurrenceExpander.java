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
 * This class is responsible for expanding a set of items with annual recurrence rules
 * into a collection of item DTOs. Each DTO represents an occurrence within a given date range.
 * The class uses a mapping function to convert items into their corresponding DTOs and manages
 * recurrence logic consistent with annual schedules.
 */
public class AnnualRecurrenceExpander {

    private final Function<Item, ItemDto> mapper;

    public AnnualRecurrenceExpander(Function<Item, ItemDto> mapper) {
        this.mapper = mapper;
    }

    /**
     * Expands a list of items with annual recurrence rules into a collection of item DTOs,
     * each representing an occurrence date within the defined ledger range.
     * The expansion considers items with specific annual schedule and filters out items
     * without valid recurrence or dates within the effective range.
     *
     * @param items       the list of {@code Item} objects to be processed
     * @param ledgerStart the start date of the ledger's effective range
     * @param ledgerEnd   the end date of the ledger's effective range
     * @return a list of {@code ItemDto} objects, each corresponding to an occurrence date
     *         within the valid range specified by the ledger
     */
    public List<ItemDto> expand(List<Item> items, LocalDate ledgerStart, LocalDate ledgerEnd) {
        List<ItemDto> expanded = new ArrayList<>();

        // Filter annual items first (periodId = 9)
        List<Item> annualItems = items.stream()
            .filter(this::isAnnual)
            .toList();

        for (Item item : annualItems) {

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

            Integer month = item.getAnnualMoy();  // Month-of-year (1–12)
            Integer day   = item.getAnnualDom();  // Day-of-month

            if (month == null || day == null) {
                // Missing anchor → skip expansion
                continue;
            }

            // Compute annual dates using the effective range
            List<LocalDate> annualDates = computeAnnualDates(effStart, effEnd, month, day);

            for (LocalDate date : annualDates) {
                ItemDto dto = mapper.apply(item);
                dto.setOccurrenceDate(date.toString());
                expanded.add(dto);
            }
        }

        return expanded;
    }

    /**
     * Determines if the given item has an annual time period.
     *
     * @param item the item to check, which may be null or have a null time period
     * @return true if the item's time period is annual (identified by ID 9), false otherwise
     */
    public boolean isAnnual(Item item) {
        if (item == null || item.getTimePeriod() == null) return false;
        int pid = Math.toIntExact(item.getTimePeriod().getId());
        return pid == 9; // annual = 9
    }

    /**
     * Computes a list of annual dates within a specified range, where each date is
     * a recurring occurrence on a particular month and day of the year.
     * If a specified date in a given year is invalid (e.g., February 30th), it will
     * be adjusted to the closest valid date (e.g., February 28th or 29th in leap years).
     *
     * @param start the start date of the range; must not be null.
     * @param end the end date of the range; must not be null and must not be before the start date.
     * @param month the month (1–12) for the recurring date; must be a valid month value.
     * @param day the day of the month for the recurring date; may exceed the maximum valid
     *            day for the specified month, in which case it will be adjusted to the last valid day.
     * @return a list of {@code LocalDate} objects representing annual occurrences
     *         within the specified range, inclusive of valid occurrences at the start and end dates.
     */
    private static List<LocalDate> computeAnnualDates(LocalDate start, LocalDate end,
                                                      int month, int day) {

        List<LocalDate> dates = new ArrayList<>();

        int startYear = start.getYear();
        int endYear   = end.getYear();

        for (int year = startYear; year <= endYear; year++) {

            LocalDate occurrence = safeDate(year, month, day);

            if (!occurrence.isBefore(start) && !occurrence.isAfter(end)) {
                dates.add(occurrence);
            }
        }

        return dates;
    }

    /**
     * Ensures that a given date is valid by adjusting the day component if it exceeds
     * the number of days in the specified month and year.
     *
     * @param year the year component of the date
     * @param month the month component of the date, where 1 represents January and 12 represents December
     * @param day the day component of the date, which may exceed the number of days in the specified month
     * @return a {@code LocalDate} instance representing a valid date, with the day adjusted to the last day
     *         of the month if it exceeds the month's length
     */
    private static LocalDate safeDate(int year, int month, int day) {
        YearMonth ym = YearMonth.of(year, month);
        int lastDay = ym.lengthOfMonth();
        int safeDay = Math.min(day, lastDay);
        return ym.atDay(safeDay);
    }
}
