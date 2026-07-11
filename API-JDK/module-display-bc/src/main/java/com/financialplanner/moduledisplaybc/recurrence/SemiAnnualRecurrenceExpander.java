package com.financialplanner.moduledisplaybc.recurrence;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import com.financialplanner.moduledisplaybc.model.ItemDto;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * A utility class for expanding a collection of items with semi-annual recurrence patterns
 * into a list of event occurrences (as {@code ItemDto} objects) within a specified date range.
 *
 * This class is designed to process items that conform to predefined semi-annual recurrence
 * rules. For eligible items, the recurrence anchors are used to calculate the exact dates of
 * occurrence, which are then mapped into {@code ItemDto} objects for further consumption.
 */
public class SemiAnnualRecurrenceExpander {

    private final Function<Item, ItemDto> mapper;

    public SemiAnnualRecurrenceExpander(Function<Item, ItemDto> mapper) {
        this.mapper = mapper;
    }

    /**
     * Expands a list of items into a list of {@code ItemDto} based on semi-annual recurrence rules
     * within the specified ledger date range.
     *
     * Only items associated with a semi-annual recurrence pattern are expanded. For each matching item,
     * occurrences are generated for the specified ledger date range using predefined semi-annual anchors.
     *
     * @param items the list of items to expand; each item may have a semi-annual recurrence pattern
     * @param ledgerStart the start date of the ledger range for generating occurrences
     * @param ledgerEnd the end date of the ledger range for generating occurrences
     * @return a list of {@code ItemDto} objects, each containing occurrence details for a single semi-annual event
     */
    public List<ItemDto> expand(List<Item> items, LocalDate ledgerStart, LocalDate ledgerEnd) {
        List<ItemDto> expanded = new ArrayList<>();

        for (Item item : items) {

            int periodId = Math.toIntExact(item.getTimePeriod() != null ? item.getTimePeriod().getId() : 0);

            // Only handle semi-annual (periodId = 8)
            if (periodId != 8) {
                continue;
            }

            // Extract the two semi-annual anchors
            Integer[] months = {
                item.getSemiAnnual1Month(),
                item.getSemiAnnual2Month()
            };

            Integer[] days = {
                item.getSemiAnnual1Day(),
                item.getSemiAnnual2Day()
            };

            int startYear = ledgerStart.getYear();
            int endYear = ledgerEnd.getYear();

            for (int year = startYear; year <= endYear; year++) {

                for (int i = 0; i < 2; i++) {
                    Integer month = months[i];
                    Integer day = days[i];

                    if (month == null || day == null) {
                        continue; // skip undefined anchors
                    }

                    LocalDate occurrence = safeDate(year, month, day);

                    if (!occurrence.isBefore(ledgerStart) && !occurrence.isAfter(ledgerEnd)) {
                        ItemDto dto = mapper.apply(item);
                        dto.setOccurrenceDate(occurrence.toString());
                        expanded.add(dto);
                    }
                }
            }
        }

        return expanded;
    }

    /**
     * Adjusts the given year, month, and day inputs to create a safe and valid {@link LocalDate}.
     *
     * If the provided day exceeds the number of days in the specified month, this method adjusts the
     * day to the last valid day of that month. For example, if the day is 31 and the month is February,
     * the method adjusts the day to 28 (or 29 in leap years).
     *
     * @param year the year to use for constructing the date
     * @param month the month to use for constructing the date (1 for January, 12 for December)
     * @param day the day of the month to use; may be clamped to the last valid day of the specified month
     * @return a {@link LocalDate} object representing the adjusted date
     */
    private LocalDate safeDate(int year, int month, int day) {
        YearMonth ym = YearMonth.of(year, month);
        int lastDay = ym.lengthOfMonth();
        int safeDay = Math.min(day, lastDay);
        return ym.atDay(safeDay);
    }

    /**
     * Determines if the given item is associated with a semi-annual recurrence pattern.
     *
     * A semi-annual pattern is identified based on the time period ID
     * of the item, which should match a predefined value (e.g., 8).
     *
     * @param item the item to check, which may contain a time period definition.
     *             If the item or its time period is null, this method returns false.
     * @return true if the item's time period ID corresponds to a semi-annual recurrence;
     *         false otherwise.
     */
    public boolean isSemiAnnual(Item item) {
        if (item == null || item.getTimePeriod() == null) return false;
        int pid = Math.toIntExact(item.getTimePeriod().getId());
        return pid == 8; // or whatever ID you assign
    }
}
