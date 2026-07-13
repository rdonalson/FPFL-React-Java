package com.financialplanner.moduledisplaybc.recurrence;

import com.financialplanner.moduledisplaybc.model.ItemDto;
import com.financialplanner.moduledisplaybc.utility.RecurrenceRange;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class WeeklyRecurrenceExpander {

    private final Function<Item, ItemDto> mapper;

    public WeeklyRecurrenceExpander(Function<Item, ItemDto> mapper) {
        this.mapper = mapper;
    }

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

    public boolean isWeekly(Item item) {
        if (item == null || item.getTimePeriod() == null) return false;
        int pid = Math.toIntExact(item.getTimePeriod().getId());
        return pid == 3;
    }

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
