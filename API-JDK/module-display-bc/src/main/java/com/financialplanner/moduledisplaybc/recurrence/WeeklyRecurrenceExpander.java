package com.financialplanner.moduledisplaybc.recurrence;

import com.financialplanner.moduledisplaybc.model.ItemDto;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * A utility class that expands weekly recurring items into a list of dated occurrences
 * represented by {@link ItemDto} objects. Non-recurring or non-weekly items are processed
 * and returned directly without generating multiple occurrences.
 *
 * This class maps {@link Item} objects to {@link ItemDto} objects using a provided mapping
 * function and considers weekly recurrence rules based on a specific day of the week.
 */
public class WeeklyRecurrenceExpander {

    private final Function<Item, ItemDto> mapper;

    public WeeklyRecurrenceExpander(Function<Item, ItemDto> mapper) {
        this.mapper = mapper;
    }

    /**
     * Expand weekly items into dated ItemDto occurrences between ledgerStart and ledgerEnd.
     * Non-weekly items are mapped once and returned.
     */
    public List<ItemDto> expand(List<Item> items, LocalDate ledgerStart, LocalDate ledgerEnd) {
        List<ItemDto> expanded = new ArrayList<>();

        for (Item item : items) {
            int periodId = Math.toIntExact(item.getTimePeriod() != null ? item.getTimePeriod().getId() : 0);

            // Weekly = periodId 3 (domain rule)
            if (periodId != 3) {
                expanded.add(mapper.apply(item));
                continue;
            }

            Integer weeklyDow = item.getWeeklyDow();
            if (weeklyDow == null) {
                // missing data: treat as single occurrence to avoid NPEs
                expanded.add(mapper.apply(item));
                continue;
            }

            DayOfWeek dow = DayOfWeek.of(weeklyDow);
            List<LocalDate> weeklyDates = computeWeeklyDates(ledgerStart, ledgerEnd, dow);

            for (LocalDate date : weeklyDates) {
                ItemDto dto = mapper.apply(item);
                dto.setOccurrenceDate(date.toString());
                expanded.add(dto);
            }
        }

        return expanded;
    }

    public boolean isWeekly(Item item) {
        if (item == null || item.getTimePeriod() == null) return false;
        int pid = Math.toIntExact(item.getTimePeriod().getId());
        return pid == 3; // weekly = 3
    }

    private static List<LocalDate> computeWeeklyDates(LocalDate start, LocalDate end, DayOfWeek targetDay) {
        List<LocalDate> dates = new ArrayList<>();

        LocalDate cursor = start;
        // advance to first matching day (safe even if start already matches)
        while (cursor.getDayOfWeek() != targetDay) {
            cursor = cursor.plusDays(1);
            if (cursor.isAfter(end)) {
                return dates; // no occurrences in range
            }
        }

        while (!cursor.isAfter(end)) {
            dates.add(cursor);
            cursor = cursor.plusWeeks(1);
        }

        return dates;
    }
}
