package com.financialplanner.moduledisplaybc.recurrence;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import com.financialplanner.moduledisplaybc.model.ItemDto;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * The QuarterlyRecurrenceExpander class provides functionality to expand a list of items
 * into occurrences based on quarterly recurrence rules. It processes items that are
 * configured with quarterly recurrence and ensures those occurrences fall within a specified
 * date range.
 */
public class QuarterlyRecurrenceExpander {

    private final Function<Item, ItemDto> mapper;

    public QuarterlyRecurrenceExpander(Function<Item, ItemDto> mapper) {
        this.mapper = mapper;
    }

    /**
     * Expands a list of items into a list of {@code ItemDto} objects by generating occurrence dates
     * based on quarterly recurrence rules within the specified ledger period.
     * Only items with a quarterly time period are processed.
     *
     * @param items the list of items to be expanded; each item may contain quarterly recurrence information
     * @param ledgerStart the start date of the ledger period; items occurring before this date are ignored
     * @param ledgerEnd the end date of the ledger period; items occurring after this date are ignored
     * @return a list of {@code ItemDto} objects, each representing an occurrence of an item within the specified ledger period
     */
    public List<ItemDto> expand(List<Item> items, LocalDate ledgerStart, LocalDate ledgerEnd) {
        List<ItemDto> expanded = new ArrayList<>();

        for (Item item : items) {

            int periodId = Math.toIntExact(item.getTimePeriod() != null ? item.getTimePeriod().getId() : 0);

            // Only handle quarterly (periodId = 7)
            if (periodId != 7) {
                continue;
            }

            // Extract the four quarterly anchors
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

            // Expand year-by-year
            int startYear = ledgerStart.getYear();
            int endYear = ledgerEnd.getYear();

            for (int year = startYear; year <= endYear; year++) {

                for (int i = 0; i < 4; i++) {
                    Integer month = months[i];
                    Integer day = days[i];

                    if (month == null || day == null) {
                        continue; // skip undefined quarterly anchors
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
     * Returns a valid LocalDate for the given year/month/day.
     * If the day exceeds the number of days in the month (e.g., 31 in February),
     * it snaps to the last day of the month.
     */
    private LocalDate safeDate(int year, int month, int day) {
        YearMonth ym = YearMonth.of(year, month);
        int lastDay = ym.lengthOfMonth();
        int safeDay = Math.min(day, lastDay);
        return ym.atDay(safeDay);
    }

    /**
     * Determines if the given item is marked as having a quarterly recurrence.
     *
     * @param item the item to evaluate; may be null
     * @return true if the item is non-null, has a time period, and its time period ID
     *         corresponds to quarterly recurrence (e.g., ID 7); false otherwise
     */
    public boolean isQuarterly(Item item) {
        if (item == null || item.getTimePeriod() == null) return false;
        int pid = Math.toIntExact(item.getTimePeriod().getId());
        return pid == 7; // or whatever ID you assign
    }
}
