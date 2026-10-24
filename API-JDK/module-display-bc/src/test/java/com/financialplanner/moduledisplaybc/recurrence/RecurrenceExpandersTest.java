package com.financialplanner.moduledisplaybc.recurrence;

import com.financialplanner.moduledisplaybc.model.ItemDto;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.TimePeriod;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/** Every recurrence pattern, through {@link RecurrenceOccurrences} (the same expanders the ledger uses). */
class RecurrenceExpandersTest {

    private final RecurrenceOccurrences occurrences = new RecurrenceOccurrences();

    private static LocalDate d(int year, int month, int day) {
        return LocalDate.of(year, month, day);
    }

    private static Item item(long period, Consumer<Item> setup) {
        Item item = new Item();
        item.setId(1L);
        item.setName("x");
        item.setAmount(10.0);
        item.setTimePeriod(new TimePeriod(period, "p" + period));
        setup.accept(item);
        return item;
    }

    private List<LocalDate> dates(Item item, LocalDate start, LocalDate end) {
        return occurrences.of(item, start, end);
    }

    @Test
    void oneTimeHappensOnItsDateOnlyWhenInRange() {
        Item bonus = item(1, i -> i.setBeginDate(d(2026, 12, 15)));
        assertEquals(List.of(d(2026, 12, 15)), dates(bonus, d(2026, 12, 1), d(2026, 12, 31)));
        assertEquals(List.of(), dates(bonus, d(2027, 1, 1), d(2027, 1, 31)));
        assertEquals(List.of(), dates(item(1, i -> {}), d(2026, 1, 1), d(2026, 12, 31))); // no date
    }

    @Test
    void dailyIsEveryDay() {
        assertEquals(List.of(d(2026, 2, 27), d(2026, 2, 28), d(2026, 3, 1)),
                     dates(item(2, i -> {}), d(2026, 2, 27), d(2026, 3, 1)));
    }

    @Nested
    class Weekly {
        @Test
        void landsOnTheChosenWeekday() {
            Item friday = item(3, i -> i.setWeeklyDow(5));
            assertEquals(List.of(d(2026, 10, 2), d(2026, 10, 9), d(2026, 10, 16)),
                         dates(friday, d(2026, 10, 1), d(2026, 10, 16)));
        }

        @Test
        void sundayWorksWhetherSavedAsZeroOrSeven() {
            // The UI saves Sunday as 0; DayOfWeek.of(0) used to throw and break the whole ledger
            List<LocalDate> sundays = List.of(d(2026, 10, 4), d(2026, 10, 11));
            assertEquals(sundays, dates(item(3, i -> i.setWeeklyDow(0)), d(2026, 10, 1), d(2026, 10, 14)));
            assertEquals(sundays, dates(item(3, i -> i.setWeeklyDow(7)), d(2026, 10, 1), d(2026, 10, 14)));
        }

        @Test
        void aMissingOrBadWeekdayIsSkipped() {
            assertEquals(List.of(), dates(item(3, i -> {}), d(2026, 10, 1), d(2026, 10, 31)));
            assertEquals(List.of(), dates(item(3, i -> i.setWeeklyDow(9)), d(2026, 10, 1), d(2026, 10, 31)));
        }
    }

    @Test
    void everyTwoWeeksOnSundayToo() {
        Item paycheck = item(4, i -> {
            i.setEveryOtherWeekDow(0);
            i.setBeginDate(d(2026, 10, 4));
        });
        assertEquals(List.of(d(2026, 10, 4), d(2026, 10, 18)), dates(paycheck, d(2026, 10, 1), d(2026, 10, 31)));
    }

    @Test
    void biMonthlyHitsBothDaysAndClampsToMonthEnd() {
        Item twice = item(5, i -> {
            i.setBiMonthlyDay1(15);
            i.setBiMonthlyDay2(31);
        });
        assertEquals(List.of(d(2026, 2, 15), d(2026, 2, 28), d(2026, 3, 15), d(2026, 3, 31)),
                     dates(twice, d(2026, 2, 1), d(2026, 3, 31)));
        assertEquals(List.of(d(2026, 3, 15)), dates(item(5, i -> i.setBiMonthlyDay1(15)), d(2026, 3, 1), d(2026, 3, 31)));
        assertEquals(List.of(), dates(item(5, i -> {}), d(2026, 3, 1), d(2026, 3, 31)));
    }

    @Test
    void monthlyClampsShortMonths() {
        Item rent = item(6, i -> i.setMonthlyDom(31));
        assertEquals(List.of(d(2026, 1, 31), d(2026, 2, 28), d(2026, 3, 31), d(2026, 4, 30)),
                     dates(rent, d(2026, 1, 1), d(2026, 4, 30)));
        assertEquals(List.of(d(2024, 2, 29)), dates(rent, d(2024, 2, 1), d(2024, 2, 29))); // leap year
        assertEquals(List.of(), dates(item(6, i -> {}), d(2026, 1, 1), d(2026, 4, 30)));
    }

    @Test
    void quarterlyUsesItsFourAnchorsAcrossYears() {
        Item taxes = item(7, i -> {
            i.setQuarterly1Month(1);
            i.setQuarterly1Day(15);
            i.setQuarterly2Month(4);
            i.setQuarterly2Day(15);
            i.setQuarterly3Month(6);
            i.setQuarterly3Day(31); // June has 30
            i.setQuarterly4Month(9);
            i.setQuarterly4Day(15);
        });
        assertEquals(List.of(d(2026, 4, 15), d(2026, 6, 30), d(2026, 9, 15), d(2027, 1, 15)),
                     dates(taxes, d(2026, 2, 1), d(2027, 3, 31)));
    }

    @Test
    void quarterlySkipsAnchorsThatAreNotSet() {
        Item partial = item(7, i -> {
            i.setQuarterly1Month(1);
            i.setQuarterly1Day(15);
        });
        assertEquals(List.of(d(2026, 1, 15)), dates(partial, d(2026, 1, 1), d(2026, 12, 31)));
    }

    @Test
    void semiAnnualTwiceAYear() {
        Item insurance = item(8, i -> {
            i.setSemiAnnual1Month(1);
            i.setSemiAnnual1Day(1);
            i.setSemiAnnual2Month(7);
            i.setSemiAnnual2Day(1);
        });
        assertEquals(List.of(d(2026, 1, 1), d(2026, 7, 1), d(2027, 1, 1)), dates(insurance, d(2026, 1, 1), d(2027, 6, 30)));
    }

    @Test
    void annualOncePerYearAndFeb29Clamps() {
        Item leap = item(9, i -> {
            i.setAnnualMoy(2);
            i.setAnnualDom(29);
        });
        assertEquals(List.of(d(2027, 2, 28), d(2028, 2, 29)), dates(leap, d(2027, 1, 1), d(2028, 12, 31)));
        assertEquals(List.of(), dates(item(9, i -> i.setAnnualMoy(2)), d(2027, 1, 1), d(2028, 12, 31)));
    }

    @Nested
    class NthWeekday {
        @Test
        void secondTuesdayOfEachMonth() {
            Item club = item(10, i -> {
                i.setNthDow(2);
                i.setNthIndex(2);
            });
            assertEquals(List.of(d(2026, 10, 13), d(2026, 11, 10), d(2026, 12, 8)), dates(club, d(2026, 10, 1), d(2026, 12, 31)));
        }

        @Test
        void lastFridayOfTheMonth() {
            Item lastFriday = item(10, i -> {
                i.setNthDow(5);
                i.setNthIndex(-1);
            });
            assertEquals(List.of(d(2026, 10, 30), d(2026, 11, 27)), dates(lastFriday, d(2026, 10, 1), d(2026, 11, 30)));
        }

        @Test
        void firstSundaySavedAsZero() {
            Item firstSunday = item(10, i -> {
                i.setNthDow(0);
                i.setNthIndex(1);
            });
            assertEquals(List.of(d(2026, 11, 1)), dates(firstSunday, d(2026, 11, 1), d(2026, 11, 30)));
        }
    }

    @Test
    void aTimeFrameLimitsTheOccurrences() {
        Item gym = item(6, i -> {
            i.setMonthlyDom(1);
            i.setDateRangeReq(true);
            i.setBeginDate(d(2026, 3, 1));
            i.setEndDate(d(2026, 5, 31));
        });
        assertEquals(List.of(d(2026, 3, 1), d(2026, 4, 1), d(2026, 5, 1)), dates(gym, d(2026, 1, 1), d(2026, 12, 31)));
        assertEquals(List.of(), dates(gym, d(2026, 7, 1), d(2026, 12, 31))); // no overlap
    }

    @Test
    void anItemWithoutAPeriodIsTreatedAsOneTime() {
        Item none = new Item();
        none.setBeginDate(d(2026, 1, 1));
        assertEquals(List.of(d(2026, 1, 1)), occurrences.of(none, d(2026, 1, 1), d(2026, 12, 31)));
        assertEquals(List.of(), occurrences.of(null, d(2026, 1, 1), d(2026, 12, 31)));
        assertEquals(List.of(), occurrences.of(item(2, i -> {}), d(2026, 2, 1), d(2026, 1, 1)));
    }

    @Test
    void eachExpanderOnlyClaimsItsOwnPeriod() {
        Item monthly = item(6, i -> i.setMonthlyDom(1));
        var map = (java.util.function.Function<Item, ItemDto>) i -> new ItemDto();
        LocalDate s = d(2026, 1, 1), e = d(2026, 1, 31);
        assertTrue(new WeeklyRecurrenceExpander(map).expand(List.of(monthly), s, e).isEmpty());
        assertTrue(new DailyRecurrenceExpander(map).expand(List.of(monthly), s, e).isEmpty());
        assertEquals(1, new MonthlyRecurrenceExpander(map).expand(List.of(monthly), s, e).size());
        assertFalse(new MonthlyRecurrenceExpander(map).isMonthly(null));
    }

    @Test
    void weekdaysAcceptsBothConventions() {
        assertEquals(DayOfWeek.SUNDAY, Weekdays.of(0));
        assertEquals(DayOfWeek.MONDAY, Weekdays.of(1));
        assertEquals(DayOfWeek.SATURDAY, Weekdays.of(6));
        assertEquals(DayOfWeek.SUNDAY, Weekdays.of(7));
        assertNull(Weekdays.of(-1));
        assertNull(Weekdays.of(8));
        assertNull(Weekdays.of(null));
    }
}
