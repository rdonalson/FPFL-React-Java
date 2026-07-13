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
 * The {@code WeeklyRecurrenceExpander} class is responsible for expanding a list of {@link Item}
 * objects into a list of {@link ItemDto} objects by generating recurring weekly dates within a
 * specified ledger range. Only items with a weekly recurrence type are considered for expansion.
 */
public class WeeklyRecurrenceExpander {

    private final Function<Item, ItemDto> mapper;

    public WeeklyRecurrenceExpander(Function<Item, ItemDto> mapper) {
        this.mapper = mapper;
    }

    /**
     * Expands a list of {@link Item} objects into a list of {@link ItemDto} objects by generating
     * recurring weekly dates within a specified ledger range. During expansion, only items with
     * a weekly recurrence type are processed, and their effective date range is resolved based
     * on the input ledger range and the item's data requirements.
     *
     * @param items       the list of items to be expanded. Only items with a weekly recurrence
     *                    type (identified by a specific time period ID) are considered.
     * @param ledgerStart the start date of the ledger period, used as the default lower bound of
     *                    the item's effective range if no specific range is resolved.
     * @param ledgerEnd   the end date of the ledger period, used as the default upper bound of
     *                    the item's effective range if no specific range is resolved.
     * @return a list of {@link ItemDto} objects representing the expanded items with their
     *         weekly recurrence dates calculated and set.
     */
    public List<ItemDto> expand(List<Item> items, LocalDate ledgerStart, LocalDate ledgerEnd) {
        List<ItemDto> expanded = new ArrayList<>();

        // Filter weekly items first (periodId = 3)
        List<Item> weeklyItems = items.stream()
            .filter(this::isWeekly)
            .toList();

        for (Item item : weeklyItems) {

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

            Integer weeklyDow = item.getWeeklyDow();
            if (weeklyDow == null) {
                // Missing data → skip weekly expansion
                continue;
            }

            DayOfWeek dow = DayOfWeek.of(weeklyDow);

            // Compute weekly dates using the effective range
            List<LocalDate> weeklyDates = computeWeeklyDates(effStart, effEnd, dow);

            for (LocalDate date : weeklyDates) {
                ItemDto dto = mapper.apply(item);
                dto.setOccurrenceDate(date.toString());
                expanded.add(dto);
            }
        }

        return expanded;
    }

    /**
     * Determines if the provided item is associated with a weekly time period.
     * An item is considered weekly if its time period is not null and its period ID equals 3.
     *
     * @param item the item to be checked for a weekly time period, may be null
     * @return true if the item's time period is weekly (period ID is 3), false otherwise or if the item or its time period is null
     */
    public boolean isWeekly(Item item) {
        if (item == null || item.getTimePeriod() == null) return false;
        int pid = Math.toIntExact(item.getTimePeriod().getId());
        return pid == 3;
    }

    /**
     * Computes a list of dates within a specified range that fall on a specific day of the week.
     *
     * @param start the start date of the range; must not be null.
     * @param end the end date of the range; must not be null and must not be earlier than the start date.
     * @param targetDay the day of the week to compute occurrences for; must not be null.
     * @return a list of {@code LocalDate} objects representing all dates within the range
     *         that fall on the specified day of the week. If no such dates exist, an empty list is returned.
     */
    private static List<LocalDate> computeWeeklyDates(LocalDate start, LocalDate end, DayOfWeek targetDay) {
        List<LocalDate> dates = new ArrayList<>();

        LocalDate cursor = start;

        // Advance to first matching weekday
        while (cursor.getDayOfWeek() != targetDay) {
            cursor = cursor.plusDays(1);
            if (cursor.isAfter(end)) {
                return dates; // no occurrences in range
            }
        }

        // Add weekly occurrences
        while (!cursor.isAfter(end)) {
            dates.add(cursor);
            cursor = cursor.plusWeeks(1);
        }

        return dates;
    }
}
