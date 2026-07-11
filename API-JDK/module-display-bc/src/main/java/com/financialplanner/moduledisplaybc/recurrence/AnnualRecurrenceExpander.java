package com.financialplanner.moduledisplaybc.recurrence;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import com.financialplanner.moduledisplaybc.model.ItemDto;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * The {@code AnnualRecurrenceExpander} class is responsible for expanding items with annual recurrence
 * into a list of occurrences within a specified date range. It evaluates and processes recurrence information
 * from input data, ensuring valid transformations into a format suitable for further operations or visualization.
 */
public class AnnualRecurrenceExpander {

    private final Function<Item, ItemDto> mapper;

    public AnnualRecurrenceExpander(Function<Item, ItemDto> mapper) {
        this.mapper = mapper;
    }

    /**
     * Expands a list of items with annual recurrence into a list of occurrences within a specified date range.
     * Each item's recurrence is evaluated to determine its occurrences between the provided ledger start
     * and ledger end dates. Single-occurrence items or invalid entries are directly mapped without expansion.
     *
     * @param items the list of {@code Item} objects to be expanded, where each item may represent an annual recurrence.
     * @param ledgerStart the start date of the ledger, determining the beginning of the range for recurrence evaluation.
     * @param ledgerEnd the end date of the ledger, determining the conclusion of the range for recurrence evaluation.
     * @return a list of {@code ItemDto} objects, each representing an occurrence of an item within the specified range.
     */
    public List<ItemDto> expand(List<Item> items, LocalDate ledgerStart, LocalDate ledgerEnd) {
        List<ItemDto> expanded = new ArrayList<>();

        for (Item item : items) {

            int periodId = Math.toIntExact(item.getTimePeriod() != null ? item.getTimePeriod().getId() : 0);

            // Only handle annual (periodId = 9)
            if (periodId != 9) {
                continue;
            }

            Integer month = item.getAnnualMoy();
            Integer day   = item.getAnnualDom();

            if (month == null || day == null) {
                // No valid anchor → treat as single occurrence
                expanded.add(mapper.apply(item));
                continue;
            }

            int startYear = ledgerStart.getYear();
            int endYear   = ledgerEnd.getYear();

            for (int year = startYear; year <= endYear; year++) {

                LocalDate occurrence = safeDate(year, month, day);

                if (!occurrence.isBefore(ledgerStart) && !occurrence.isAfter(ledgerEnd)) {
                    ItemDto dto = mapper.apply(item);
                    dto.setOccurrenceDate(occurrence.toString());
                    expanded.add(dto);
                }
            }
        }

        return expanded;
    }

    /**
     * Ensures that the specified day of the given year and month is safe and valid.
     * If the provided day exceeds the last day of the month, it is adjusted to the month's last valid day.
     *
     * @param year the year for which the date is determined
     * @param month the month (1-12) for which the date is determined
     * @param day the desired day, which may exceed the month's last day
     * @return a {@code LocalDate} representing a safe and valid date in the specified year and month,
     *         adjusted if necessary to ensure the day does not exceed the month's last valid day
     */
    private LocalDate safeDate(int year, int month, int day) {
        YearMonth ym = YearMonth.of(year, month);
        int lastDay = ym.lengthOfMonth();
        int safeDay = Math.min(day, lastDay);
        return ym.atDay(safeDay);
    }

    /**
     * Determines if the given item has an annual recurrence.
     *
     * @param item the item to evaluate; may be null.
     * @return {@code true} if the item's time period indicates annual recurrence (e.g., periodId = 9),
     *         {@code false} otherwise or if the item or its time period is null.
     */
    public boolean isAnnual(Item item) {
        if (item == null || item.getTimePeriod() == null) return false;
        int pid = Math.toIntExact(item.getTimePeriod().getId());
        return pid == 9; // or whatever ID you assign
    }
}
