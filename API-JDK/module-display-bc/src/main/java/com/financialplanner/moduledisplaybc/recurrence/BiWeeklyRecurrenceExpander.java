package com.financialplanner.moduledisplaybc.recurrence;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import com.financialplanner.moduledisplaybc.model.ItemDto;

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

    /**
     * Bi-weekly recurrence (periodId = 4)
     * - beginDate may be up to 6 days before the target weekday
     * - First occurrence = first target weekday AFTER beginDate
     * - Subsequent occurrences = every 14 days
     */
    public List<ItemDto> expand(List<Item> items, LocalDate ledgerStart, LocalDate ledgerEnd) {
        List<ItemDto> expanded = new ArrayList<>();

        for (Item item : items) {

            int periodId = Math.toIntExact(item.getTimePeriod() != null ? item.getTimePeriod().getId() : 0);

            // Only handle bi-weekly (periodId = 4)
            if (periodId != 4) {
                expanded.add(mapper.apply(item));
                continue;
            }

            LocalDate begin = item.getBeginDate();
            if (begin == null) {
                // No begin date → treat as single occurrence
                expanded.add(mapper.apply(item));
                continue;
            }

            // Determine target weekday (same as everyOtherWeekDow)
            Integer everyOtherWeekDow = item.getEveryOtherWeekDow();
            if (everyOtherWeekDow == null) {
                // No weekday → treat as single occurrence
                expanded.add(mapper.apply(item));
                continue;
            }

            DayOfWeek targetDow = DayOfWeek.of(everyOtherWeekDow);

            // Step 1: find first target weekday AFTER begin date
            LocalDate firstOccurrence = snapToNextWeekday(begin, targetDow);

            // Step 2: generate bi-weekly occurrences (every 14 days)
            List<LocalDate> occurrences = computeBiWeeklyDates(firstOccurrence, ledgerStart, ledgerEnd);

            // Step 3: map each occurrence
            for (LocalDate date : occurrences) {
                ItemDto dto = mapper.apply(item);
                dto.setOccurrenceDate(date.toString());
                expanded.add(dto);
            }
        }

        return expanded;
    }

    /**
     * Snap forward to the first target weekday AFTER the given date.
     */
    private LocalDate snapToNextWeekday(LocalDate start, DayOfWeek targetDow) {
        LocalDate cursor = start;
        while (cursor.getDayOfWeek() != targetDow) {
            cursor = cursor.plusDays(1);
        }
        return cursor;
    }

    /**
     * Generate bi-weekly (14-day) occurrences between ledgerStart and ledgerEnd.
     */
    private List<LocalDate> computeBiWeeklyDates(LocalDate first, LocalDate ledgerStart, LocalDate ledgerEnd) {
        List<LocalDate> dates = new ArrayList<>();

        // If first occurrence is before ledgerStart, advance by 14-day increments
        LocalDate cursor = first;
        while (cursor.isBefore(ledgerStart)) {
            cursor = cursor.plusDays(14);
        }

        // Collect occurrences within range
        while (!cursor.isAfter(ledgerEnd)) {
            dates.add(cursor);
            cursor = cursor.plusDays(14);
        }

        return dates;
    }

    public boolean isBiWeekly(Item item) {
        if (item == null || item.getTimePeriod() == null) return false;
        int pid = Math.toIntExact(item.getTimePeriod().getId());
        return pid == 4;
    }
}
