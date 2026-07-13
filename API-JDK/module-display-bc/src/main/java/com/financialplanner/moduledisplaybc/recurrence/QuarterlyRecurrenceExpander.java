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
 * A utility class for expanding a collection of items into their quarterly occurrences
 * based on predefined rules and a specified ledger range. This class processes items
 * flagged as quarterly and calculates their occurrences within an effective date range.
 * The resulting occurrences are mapped and returned as DTO objects.
 */
public class QuarterlyRecurrenceExpander {

    private final Function<Item, ItemDto> mapper;

    public QuarterlyRecurrenceExpander(Function<Item, ItemDto> mapper) {
        this.mapper = mapper;
    }

    /**
     * Expands a list of items into their quarterly occurrences based on a specified ledger range.
     * Only items that are defined as quarterly are processed, and their occurrences are calculated
     * within the effective range determined by either the default ledger range or a resolved date range.
     *
     * @param items the list of items to be expanded
     * @param ledgerStart the start date of the ledger range
     * @param ledgerEnd the end date of the ledger range
     * @return a list of {@code ItemDto} objects representing the expanded items with their occurrences
     */
    public List<ItemDto> expand(List<Item> items, LocalDate ledgerStart, LocalDate ledgerEnd) {
        List<ItemDto> expanded = new ArrayList<>();

        // Filter quarterly items first (periodId = 7)
        List<Item> quarterlyItems = items.stream()
            .filter(this::isQuarterly)
            .toList();

        for (Item item : quarterlyItems) {

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

            // Extract quarterly anchors
            Integer[] months = {
                item.getQuarterly1Month(),
                item.getQuarterly2Month(),
                item.getQuarterly3Month(),
                item.getQuarterly4Month()
            };

            Integer[] days = {
                item.getQuarterly1Day(),
                item.getQuarterly2Day(),
                item.getQuarterly3Day(),
                item.getQuarterly4Day()
            };

            // Compute quarterly dates using the effective range
            List<LocalDate> quarterlyDates = computeQuarterlyDates(effStart, effEnd, months, days);

            for (LocalDate date : quarterlyDates) {
                ItemDto dto = mapper.apply(item);
                dto.setOccurrenceDate(date.toString());
                expanded.add(dto);
            }
        }

        return expanded;
    }

    /**
     * Determines if the given item has a quarterly recurrence.
     *
     * @param item the item to be checked; must not be null and must have a time period associated with it for the method to return true.
     * @return true if the item's time period ID is 7 (quarterly), false otherwise or if the item or its time period is null.
     */
    public boolean isQuarterly(Item item) {
        if (item == null || item.getTimePeriod() == null) return false;
        int pid = Math.toIntExact(item.getTimePeriod().getId());
        return pid == 7; // quarterly = 7
    }

    /**
     * Computes the quarterly occurrence dates within the specified date range, based on the
     * provided months and days for each quarter.
     *
     * This method calculates a list of dates for quarterly occurrences by iterating through each
     * year within the specified range. For each quarter, it uses the month and day values provided
     * to determine the exact date of the occurrence. If the computed date falls within the given
     * range, it is included in the resulting list.
     *
     * @param start the start date of the range; must not be null.
     * @param end the end date of the range; must not be null and must not be before the start date.
     * @param months an array of integers representing the months for each quarter; must have a length of 4.
     *               Null values indicate skipped quarters.
     * @param days an array of integers representing the days for each quarter; must have a length of 4.
     *             Null values indicate skipped quarters.
     * @return a list of {@code LocalDate} objects representing all computed quarterly dates within the specified range.
     */
    private static List<LocalDate> computeQuarterlyDates(LocalDate start, LocalDate end,
                                                         Integer[] months, Integer[] days) {

        List<LocalDate> dates = new ArrayList<>();

        int startYear = start.getYear();
        int endYear   = end.getYear();

        for (int year = startYear; year <= endYear; year++) {

            for (int i = 0; i < 4; i++) {
                Integer month = months[i];
                Integer day   = days[i];

                if (month == null || day == null) {
                    continue; // skip undefined quarterly anchors
                }

                LocalDate occurrence = safeDate(year, month, day);

                if (!occurrence.isBefore(start) && !occurrence.isAfter(end)) {
                    dates.add(occurrence);
                }
            }
        }

        return dates;
    }

    /**
     * Safely creates a {@code LocalDate} object by adjusting the day of the month to ensure
     * it falls within the valid range for the specified year and month.
     *
     * @param year the year component of the date
     * @param month the month component of the date (1-based, i.e., 1 for January, 12 for December)
     * @param day the day component of the date, which will be clamped to the last valid day if it exceeds the month's length
     * @return a {@code LocalDate} object representing the adjusted date
     */
    private static LocalDate safeDate(int year, int month, int day) {
        YearMonth ym = YearMonth.of(year, month);
        int lastDay = ym.lengthOfMonth();
        int safeDay = Math.min(day, lastDay);
        return ym.atDay(safeDay);
    }
}
