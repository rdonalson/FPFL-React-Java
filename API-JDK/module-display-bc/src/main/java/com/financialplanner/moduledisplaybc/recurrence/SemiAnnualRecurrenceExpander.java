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
 * The {@code SemiAnnualRecurrenceExpander} class provides functionality to expand a list of items
 * into their corresponding semi-annual occurrences within a specified date range. It processes
 * items configured for semi-annual recurrence and computes their occurrence dates based on the
 * defined recurrence rules.
 * The class filters items for a semi-annual recurrence type, resolves the applicable effective
 * date range, and computes occurrence dates that fall within the specified range. The resulting
 * occurrences are returned as a list of data transfer objects (DTOs) with the specific occurrence
 * dates.
 */
public class SemiAnnualRecurrenceExpander {

    private final Function<Item, ItemDto> mapper;

    public SemiAnnualRecurrenceExpander(Function<Item, ItemDto> mapper) {
        this.mapper = mapper;
    }

    /**
     * Expands a list of items into their corresponding semi-annual occurrences within a specified date range.
     * Filters and processes semi-annual items, resolves their effective date ranges, and computes their
     * semi-annual occurrence dates. Returns a list of corresponding DTOs for the computed occurrences.
     *
     * @param items the list of {@link Item} objects to be expanded
     * @param ledgerStart the start date of the ledger range
     * @param ledgerEnd the end date of the ledger range
     * @return a list of {@link ItemDto} objects corresponding to the computed semi-annual occurrences
     */
    public List<ItemDto> expand(List<Item> items, LocalDate ledgerStart, LocalDate ledgerEnd) {
        List<ItemDto> expanded = new ArrayList<>();

        // Filter semi-annual items first (periodId = 8)
        List<Item> semiAnnualItems = items.stream()
            .filter(this::isSemiAnnual)
            .toList();

        for (Item item : semiAnnualItems) {

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

            // Extract semi-annual anchors
            Integer[] months = {
                item.getSemiAnnual1Month(),
                item.getSemiAnnual2Month()
            };

            Integer[] days = {
                item.getSemiAnnual1Day(),
                item.getSemiAnnual2Day()
            };

            // Compute semi-annual dates using the effective range
            List<LocalDate> semiAnnualDates = computeSemiAnnualDates(effStart, effEnd, months, days);

            for (LocalDate date : semiAnnualDates) {
                ItemDto dto = mapper.apply(item);
                dto.setOccurrenceDate(date.toString());
                expanded.add(dto);
            }
        }

        return expanded;
    }

    /**
     * Determines whether the given item is associated with a semi-annual time period.
     *
     * @param item The item to evaluate. It may be null or have a null time period.
     * @return {@code true} if the item's time period corresponds to semi-annual (ID = 8),
     *         otherwise {@code false}.
     */
    public boolean isSemiAnnual(Item item) {
        if (item == null || item.getTimePeriod() == null) return false;
        int pid = Math.toIntExact(item.getTimePeriod().getId());
        return pid == 8; // semi-annual = 8
    }

    /**
     * Computes a list of semi-annual dates within a specified range, based on the
     * given months and days for each occurrence in a year. The method verifies
     * that each computed date falls within the specified start and end range.
     *
     * @param start the start date of the range; must not be null.
     * @param end the end date of the range; must not be null and must not be before the start date.
     * @param months an array of two integers representing the months of the semi-annual occurrences.
     *        Each value must be a valid month (1-12) or null if skipped.
     * @param days an array of two integers representing the days of the semi-annual occurrences.
     *        Each value must be a valid day (1-31) or null if skipped. Days exceeding the valid
     *        number of days in the month are adjusted to the last valid day of the month.
     * @return a list of {@code LocalDate} objects representing the semi-annual dates that
     *         fall within the specified range. The list is empty if no dates meet the conditions.
     */
    private static List<LocalDate> computeSemiAnnualDates(LocalDate start, LocalDate end,
                                                          Integer[] months, Integer[] days) {

        List<LocalDate> dates = new ArrayList<>();

        int startYear = start.getYear();
        int endYear   = end.getYear();

        for (int year = startYear; year <= endYear; year++) {

            for (int i = 0; i < 2; i++) {
                Integer month = months[i];
                Integer day   = days[i];

                if (month == null || day == null) {
                    continue; // skip undefined anchors
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
     * Ensures that the provided day value is within the valid range for the given year and month.
     * If the specified day exceeds the number of days in the month, it will be adjusted to the
     * last valid day of the month.
     *
     * @param year the year part of the desired date
     * @param month the month part of the desired date (1-based; e.g., 1 for January, 12 for December)
     * @param day the day part of the desired date
     * @return a {@code LocalDate} representing a valid date with the given year, month, and day values,
     *         where the day is adjusted if necessary to fit the number of days in the month
     */
    private static LocalDate safeDate(int year, int month, int day) {
        YearMonth ym = YearMonth.of(year, month);
        int lastDay = ym.lengthOfMonth();
        int safeDay = Math.min(day, lastDay);
        return ym.atDay(safeDay);
    }
}
