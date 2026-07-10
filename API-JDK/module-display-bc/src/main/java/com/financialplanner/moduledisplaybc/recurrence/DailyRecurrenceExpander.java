package com.financialplanner.moduledisplaybc.recurrence;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import com.financialplanner.moduledisplaybc.model.ItemDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class DailyRecurrenceExpander {

    private final Function<Item, ItemDto> mapper;

    public DailyRecurrenceExpander(Function<Item, ItemDto> mapper) {
        this.mapper = mapper;
    }

    /**
     * Expand daily items into dated ItemDto occurrences between ledgerStart and ledgerEnd.
     * Non-daily items are mapped once and returned.
     */
    public List<ItemDto> expand(List<Item> items, LocalDate ledgerStart, LocalDate ledgerEnd) {
        List<ItemDto> expanded = new ArrayList<>();

        for (Item item : items) {
            int periodId = Math.toIntExact(item.getTimePeriod() != null ? item.getTimePeriod().getId() : 0);

            // Daily = periodId 2
            if (periodId != 2) {
                expanded.add(mapper.apply(item));
                continue;
            }

            // Determine recurrence window:
            // start = max(item.beginDate, ledgerStart)
            // end   = min(item.endDate (if present), ledgerEnd)
            LocalDate itemBegin = item.getBeginDate();
            LocalDate itemEnd = item.getEndDate();

            LocalDate start = itemBegin != null && itemBegin.isAfter(ledgerStart) ? itemBegin : ledgerStart;
            LocalDate end = (itemEnd != null && itemEnd.isBefore(ledgerEnd)) ? itemEnd : ledgerEnd;

            if (start == null || end == null || start.isAfter(end)) {
                // No valid occurrences in range — treat as single mapped occurrence (or skip)
                // To preserve previous behavior, map once (occurrenceDate from beginDate if present)
                ItemDto single = mapper.apply(item);
                if (itemBegin != null && !itemBegin.isBefore(ledgerStart) && !itemBegin.isAfter(ledgerEnd)) {
                    single.setOccurrenceDate(itemBegin.toString());
                }
                expanded.add(single);
                continue;
            }

            // Generate daily dates between start and end (inclusive)
            List<LocalDate> dates = computeDailyDates(start, end);

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
        return pid == 2; // daily = 2
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
