package com.financialplanner.moduledisplaybc.recurrence;

import com.financialplanner.moduledisplaybc.model.ItemDto;
import com.financialplanner.moduledisplaybc.utility.RecurrenceRange;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class DailyRecurrenceExpander {

    private final Function<Item, ItemDto> mapper;

    public DailyRecurrenceExpander(Function<Item, ItemDto> mapper) {
        this.mapper = mapper;
    }

    public List<ItemDto> expand(List<Item> items, LocalDate ledgerStart, LocalDate ledgerEnd) {
        List<ItemDto> expanded = new ArrayList<>();

        // Filter daily items first (periodId = 2)
        List<Item> dailyItems = items.stream()
            .filter(this::isDaily)
            .toList();

        for (Item item : dailyItems) {

            // Default range = ledger range
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

            // Expand daily occurrences using the effective range
            List<LocalDate> dates = computeDailyDates(effStart, effEnd);

            for (LocalDate date : dates) {
                ItemDto dto = mapper.apply(item);
                dto.setOccurrenceDate(date.toString());
                expanded.add(dto);
            }
        }

        return expanded;
    }

    public boolean isDaily(Item item) {
        if (item == null || item.getTimePeriod() == null) return false;
        int pid = Math.toIntExact(item.getTimePeriod().getId());
        return pid == 2;
    }

    private static List<LocalDate> computeDailyDates(LocalDate start, LocalDate end) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate cursor = start;

        while (!cursor.isAfter(end)) {
            dates.add(cursor);
            cursor = cursor.plusDays(1);
        }

        return dates;
    }
}
