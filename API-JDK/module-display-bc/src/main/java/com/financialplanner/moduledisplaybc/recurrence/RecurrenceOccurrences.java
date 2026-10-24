package com.financialplanner.moduledisplaybc.recurrence;

import com.financialplanner.moduledisplaybc.model.ItemDto;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Every date one item occurs on within a range, using the same expanders as the ledger, so Debt Paydown's
 * projection and the ledger always agree on when a credit or debit happens.
 */
public class RecurrenceOccurrences {

    private final OneTimeOccurrenceExpander oneTime;
    private final DailyRecurrenceExpander daily;
    private final WeeklyRecurrenceExpander weekly;
    private final BiWeeklyRecurrenceExpander biWeekly;
    private final BiMonthlyRecurrenceExpander biMonthly;
    private final MonthlyRecurrenceExpander monthly;
    private final QuarterlyRecurrenceExpander quarterly;
    private final SemiAnnualRecurrenceExpander semiAnnual;
    private final AnnualRecurrenceExpander annual;
    private final NthWeekdayRecurrenceExpander nthWeekday;

    public RecurrenceOccurrences() {
        Function<Item, ItemDto> mapper = item -> new ItemDto();
        oneTime    = new OneTimeOccurrenceExpander(mapper);
        daily      = new DailyRecurrenceExpander(mapper);
        weekly     = new WeeklyRecurrenceExpander(mapper);
        biWeekly   = new BiWeeklyRecurrenceExpander(mapper);
        biMonthly  = new BiMonthlyRecurrenceExpander(mapper);
        monthly    = new MonthlyRecurrenceExpander(mapper);
        quarterly  = new QuarterlyRecurrenceExpander(mapper);
        semiAnnual = new SemiAnnualRecurrenceExpander(mapper);
        annual     = new AnnualRecurrenceExpander(mapper);
        nthWeekday = new NthWeekdayRecurrenceExpander(mapper);
    }

    /** The dates {@code item} occurs on from {@code start} to {@code end} (inclusive), in order. */
    public List<LocalDate> of(Item item, LocalDate start, LocalDate end) {
        if (item == null || start == null || end == null || end.isBefore(start)) {
            return List.of();
        }

        List<Item> one = List.of(item);
        List<ItemDto> all = new ArrayList<>();
        all.addAll(oneTime.expand(one, start, end));
        all.addAll(daily.expand(one, start, end));
        all.addAll(weekly.expand(one, start, end));
        all.addAll(biWeekly.expand(one, start, end));
        all.addAll(biMonthly.expand(one, start, end));
        all.addAll(monthly.expand(one, start, end));
        all.addAll(quarterly.expand(one, start, end));
        all.addAll(semiAnnual.expand(one, start, end));
        all.addAll(annual.expand(one, start, end));
        all.addAll(nthWeekday.expand(one, start, end));

        return all.stream()
                  .map(ItemDto::getOccurrenceDate)
                  .filter(d -> d != null)
                  .map(LocalDate::parse)
                  .sorted()
                  .toList();
    }
}
