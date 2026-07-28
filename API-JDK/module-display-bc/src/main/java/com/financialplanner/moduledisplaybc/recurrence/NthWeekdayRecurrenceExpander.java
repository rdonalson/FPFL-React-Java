package com.financialplanner.moduledisplaybc.recurrence;

import com.financialplanner.moduledisplaybc.model.ItemDto;
import com.financialplanner.moduledisplaybc.utility.RecurrenceRange;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class NthWeekdayRecurrenceExpander {

    private final Function<Item, ItemDto> mapper;

    public NthWeekdayRecurrenceExpander(Function<Item, ItemDto> mapper) {
        this.mapper = mapper;
    }

    public List<ItemDto> expand(List<Item> items, LocalDate ledgerStart, LocalDate ledgerEnd) {
        List<ItemDto> expanded = new ArrayList<>();

        // Filter items with periodId = 4 (Nth weekday monthly)
        List<Item> nthItems = items.stream()
            .filter(this::isNthWeekday)
            .toList();

        for (Item item : nthItems) {

            // Default effective range = ledger range
            LocalDate effStart = ledgerStart;
            LocalDate effEnd   = ledgerEnd;

            Boolean req = item.getDateRangeReq();
            if (req != null && req) {
                LocalDate[] range = RecurrenceRange.resolveRange(item, ledgerStart, ledgerEnd);
                if (range == null) continue;
                effStart = range[0];
                effEnd   = range[1];
            }

            Integer dowVal   = item.getNthDow();
            Integer indexVal = item.getNthIndex();

            if (dowVal == null || indexVal == null) continue;

            DayOfWeek dow = DayOfWeek.of(dowVal);

            List<LocalDate> dates = computeNthWeekdayDates(effStart, effEnd, dow, indexVal);

            for (LocalDate date : dates) {
                ItemDto dto = mapper.apply(item);
                dto.setOccurrenceDate(date.toString());
                expanded.add(dto);
            }
        }

        return expanded;
    }

    public boolean isNthWeekday(Item item) {
        if (item == null || item.getTimePeriod() == null) return false;
        int pid = Math.toIntExact(item.getTimePeriod().getId());
        return pid == 10; // Nth-Weekday monthly recurrence
    }

    private static List<LocalDate> computeNthWeekdayDates(LocalDate start, LocalDate end,
                                                          DayOfWeek dow, int index) {

        List<LocalDate> dates = new ArrayList<>();

        LocalDate cursor = start.withDayOfMonth(1);

        while (!cursor.isAfter(end)) {

            LocalDate occurrence = nthWeekdayOfMonth(
                cursor.getYear(),
                cursor.getMonthValue(),
                dow,
                index
            );

            if (!occurrence.isBefore(start) && !occurrence.isAfter(end)) {
                dates.add(occurrence);
            }

            cursor = cursor.plusMonths(1);
        }

        return dates;
    }

    private static LocalDate nthWeekdayOfMonth(int year, int month, DayOfWeek dow, int index) {
        LocalDate firstOfMonth = LocalDate.of(year, month, 1);

        int shift = dow.getValue() - firstOfMonth.getDayOfWeek().getValue();
        if (shift < 0) shift += 7;

        LocalDate firstOccurrence = firstOfMonth.plusDays(shift);

        if (index > 0) {
            return firstOccurrence.plusWeeks(index - 1);
        }

        // index == -1 → last weekday of the month
        LocalDate lastOfMonth = firstOfMonth.withDayOfMonth(firstOfMonth.lengthOfMonth());
        int reverseShift = lastOfMonth.getDayOfWeek().getValue() - dow.getValue();
        if (reverseShift < 0) reverseShift += 7;

        return lastOfMonth.minusDays(reverseShift);
    }
}
