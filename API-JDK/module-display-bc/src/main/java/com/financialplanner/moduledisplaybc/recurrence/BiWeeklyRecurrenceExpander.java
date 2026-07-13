package com.financialplanner.moduledisplaybc.recurrence;

import com.financialplanner.moduledisplaybc.model.ItemDto;
import com.financialplanner.moduledisplaybc.utility.RecurrenceRange;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class BiWeeklyRecurrenceExpander {

    private final Function<Item, ItemDto> mapper;

    public BiWeeklyRecurrenceExpander(Function<Item, ItemDto> mapper) {
        this.mapper = mapper;
    }

    public List<ItemDto> expand(List<Item> items, LocalDate ledgerStart, LocalDate ledgerEnd) {
        List<ItemDto> expanded = new ArrayList<>();

        // Filter bi-weekly items first (periodId = 4)
        List<Item> biWeeklyItems = items.stream()
            .filter(this::isBiWeekly)
            .toList();

        for (Item item : biWeeklyItems) {

            LocalDate begin = item.getBeginDate();
            // Default effective range = ledger range
            LocalDate effStart = begin.isAfter(ledgerStart) ? begin : ledgerStart;
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

            // Determine target weekday (everyOtherWeekDow)
            Integer dowValue = item.getEveryOtherWeekDow();
            if (dowValue == null) {
                // Missing weekday → skip expansion
                continue;
            }

            DayOfWeek targetDow = DayOfWeek.of(dowValue);

            // Compute bi-weekly dates using the effective range
            List<LocalDate> biWeeklyDates = computeBiWeeklyDates(effStart, effEnd, targetDow);

            for (LocalDate date : biWeeklyDates) {
                ItemDto dto = mapper.apply(item);
                dto.setOccurrenceDate(date.toString());
                expanded.add(dto);
            }
        }

        return expanded;
    }

    public boolean isBiWeekly(Item item) {
        if (item == null || item.getTimePeriod() == null) return false;
        int pid = Math.toIntExact(item.getTimePeriod().getId());
        return pid == 4; // bi-weekly = 4
    }

    /**
     * Weekly-style helper, but increments by 2 weeks.
     */
    private static List<LocalDate> computeBiWeeklyDates(LocalDate start, LocalDate end, DayOfWeek targetDay) {
        List<LocalDate> dates = new ArrayList<>();

        LocalDate cursor = start;

        // Snap forward to first matching weekday
        while (cursor.getDayOfWeek() != targetDay) {
            cursor = cursor.plusDays(1);
            if (cursor.isAfter(end)) {
                return dates; // no occurrences in range
            }
        }

        // Add bi-weekly occurrences
        while (!cursor.isAfter(end)) {
            dates.add(cursor);
            cursor = cursor.plusWeeks(2);
        }

        return dates;
    }
}
