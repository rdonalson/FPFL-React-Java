package com.financialplanner.moduleapi.mappers;

import com.financialplanner.moduleapi.dtos.assistant.Direction;
import com.financialplanner.moduleapi.dtos.assistant.InitialAmountDraft;
import com.financialplanner.moduleapi.dtos.assistant.ItemDraft;
import com.financialplanner.moduleapi.dtos.assistant.OnMonthDay;
import com.financialplanner.moduleapi.dtos.initialamount.InitialAmountRequest;
import com.financialplanner.moduleapi.dtos.item.ItemRequest;
import com.financialplanner.modulecommonbc.exception.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Evaluation suite for {@link ItemDraftTranslator}.
 * <p>
 * Each test is named after something a user might actually type into the assistant. That is
 * the point: this file is the regression net for prompt changes. When a prompt edit makes
 * the model draft "every other Friday" as WEEKLY instead of EVERY_TWO_WEEKS, the failure
 * shows up here rather than in a user's ledger.
 * <p>
 * These tests exercise the translator directly, with drafts written by hand. They are fast
 * and deterministic and cost nothing to run. Testing the model's ability to produce the
 * right draft is a separate, slower suite that calls the live assistant.
 */
class ItemDraftTranslatorTest {

    private static final UUID USER = UUID.fromString("11111111-2222-3333-4444-555555555555");

    private final ItemDraftTranslator translator = new ItemDraftTranslator();

    // ---------------------------------------------------------------- accepted drafts

    @Test
    @DisplayName("\"Rent is 1850 on the first\"")
    void monthlyDebit() {
        ItemRequest request = translator.toItemRequest(
            draft("Rent", 1850.0, Direction.DEBIT, Recurrences.monthly(1)), USER);

        assertEquals(2, request.fkItemType(), "DEBIT maps to item type 2");
        assertEquals(6, request.fkPeriod(), "MONTHLY maps to period 6");
        assertEquals(1, request.monthlyDom());
        assertNull(request.weeklyDow(), "a monthly item must not carry weekday fields");
        assertNull(request.annualMoy(), "a monthly item must not carry annual fields");
    }

    @Test
    @DisplayName("\"I get paid 2400 every other Friday\"")
    void biWeeklyCredit() {
        ItemRequest request = translator.toItemRequest(
            draft("Paycheck", 2400.0, Direction.CREDIT, Recurrences.everyTwoWeeks(DayOfWeek.FRIDAY)), USER);

        assertEquals(1, request.fkItemType(), "CREDIT maps to item type 1");
        assertEquals(4, request.fkPeriod());
        assertEquals(5, request.everyOtherWeekDow(), "Friday is 5 in ISO-8601");
        assertNull(request.weeklyDow(), "bi-weekly uses everyOtherWeekDow, not weeklyDow");
    }

    @Test
    @DisplayName("\"Every Sunday\" uses ISO 7, not the UI's 0")
    void weeklyOnSunday() {
        ItemRequest request = translator.toItemRequest(
            draft("Allowance", 50.0, Direction.DEBIT, Recurrences.weekly(DayOfWeek.SUNDAY)), USER);

        assertEquals(7, request.weeklyDow(),
            "the expanders call DayOfWeek.of(), which rejects 0 and reads 7 as Sunday");
    }

    @Test
    @DisplayName("\"Paid on the 15th and the last day\"")
    void biMonthly() {
        ItemRequest request = translator.toItemRequest(
            draft("Payroll", 1200.0, Direction.CREDIT, Recurrences.biMonthly(15, 31)), USER);

        assertEquals(5, request.fkPeriod());
        assertEquals(15, request.biMonthlyDay1());
        assertEquals(31, request.biMonthlyDay2(), "the expander clamps 31 to the month's real last day");
    }

    @Test
    @DisplayName("\"Car payment, first Monday of the month\"")
    void nthWeekday() {
        ItemRequest request = translator.toItemRequest(
            draft("Car", 410.0, Direction.DEBIT, Recurrences.nthWeekday(DayOfWeek.MONDAY, 1)), USER);

        assertEquals(10, request.fkPeriod());
        assertEquals(1, request.nthDow(), "Monday is 1 in ISO-8601");
        assertEquals(1, request.nthIndex());
    }

    @Test
    @DisplayName("\"Cleaner comes the last Friday of the month\"")
    void lastWeekdayOfMonth() {
        ItemRequest request = translator.toItemRequest(
            draft("Cleaner", 120.0, Direction.DEBIT, Recurrences.nthWeekday(DayOfWeek.FRIDAY, -1)), USER);

        assertEquals(5, request.nthDow());
        assertEquals(-1, request.nthIndex(), "-1 is the expander's sentinel for the last occurrence");
    }

    @Test
    @DisplayName("Quarterly pairs are ordered, however the model lists them")
    void quarterlyIsSorted() {
        ItemRequest request = translator.toItemRequest(
            draft("Estimated tax", 900.0, Direction.DEBIT,
                  Recurrences.quarterly(pair(9, 15), pair(1, 15), pair(6, 15), pair(4, 15))), USER);

        assertEquals(1, request.quarterly1Month());
        assertEquals(4, request.quarterly2Month());
        assertEquals(6, request.quarterly3Month());
        assertEquals(9, request.quarterly4Month());
    }

    @Test
    @DisplayName("\"Insurance, 900, twice a year\" once the months are known")
    void semiAnnual() {
        ItemRequest request = translator.toItemRequest(
            draft("Insurance", 900.0, Direction.DEBIT, Recurrences.semiAnnual(pair(11, 1), pair(5, 1))), USER);

        assertEquals(8, request.fkPeriod());
        assertEquals(5, request.semiAnnual1Month());
        assertEquals(11, request.semiAnnual2Month());
    }

    @Test
    @DisplayName("\"Bonus of 5000 in March\"")
    void annual() {
        ItemRequest request = translator.toItemRequest(
            draft("Bonus", 5000.0, Direction.CREDIT, Recurrences.annual(pair(3, 15))), USER);

        assertEquals(9, request.fkPeriod());
        assertEquals(3, request.annualMoy());
        assertEquals(15, request.annualDom());
    }

    @Test
    @DisplayName("A one-time item is stored as a bounded range with only a begin date")
    void oneTime() {
        LocalDate when = LocalDate.of(2026, 10, 9);
        ItemRequest request = translator.toItemRequest(
            draft("Deposit", 300.0, Direction.CREDIT, Recurrences.oneTime(when)), USER);

        assertEquals(1, request.fkPeriod());
        assertEquals(when, request.beginDate());
        assertNull(request.endDate(), "OneTimeForm sends a begin date and no end date");
        assertTrue(request.dateRangeReq());
    }

    @Test
    @DisplayName("The user id comes from the caller, never from the draft")
    void userIdIsAttachedByTheCaller() {
        ItemRequest request = translator.toItemRequest(
            draft("Coffee", 6.0, Direction.DEBIT, Recurrences.daily()), USER);

        assertEquals(USER, request.userId());
    }

    @Test
    @DisplayName("A starting balance may be negative")
    void negativeInitialAmountIsAllowed() {
        InitialAmountRequest request =
            translator.toInitialAmountRequest(new InitialAmountDraft(-250.0), USER);

        assertEquals(-250.0, request.amount());
        assertEquals(USER, request.userId());
    }

    // ---------------------------------------------------------------- rejected drafts

    @Test
    @DisplayName("\"Twice a month\" with no days is not enough to draft")
    void biMonthlyNeedsBothDays() {
        assertRejected(draft("Something", 100.0, Direction.DEBIT, Recurrences.biMonthly(null, null)));
    }

    @Test
    @DisplayName("Bi-monthly needs two different days")
    void biMonthlyNeedsDistinctDays() {
        assertRejected(draft("Something", 100.0, Direction.DEBIT, Recurrences.biMonthly(15, 15)));
    }

    @Test
    @DisplayName("A negative amount is a direction mistake, not a valid item")
    void amountMustBePositive() {
        assertRejected(draft("Refund", -20.0, Direction.CREDIT, Recurrences.daily()));
    }

    @Test
    @DisplayName("There is no seventh week in a month")
    void weekOfMonthIsBounded() {
        assertRejected(draft("Odd", 10.0, Direction.DEBIT, Recurrences.nthWeekday(DayOfWeek.MONDAY, 7)));
    }

    @Test
    @DisplayName("Semi-annual means exactly two month/day pairs")
    void semiAnnualNeedsTwoPairs() {
        assertRejected(draft("Odd", 10.0, Direction.DEBIT,
            Recurrences.semiAnnual(pair(1, 1), pair(5, 1), pair(9, 1))));
    }

    @Test
    @DisplayName("A one-time item without a date is not draftable")
    void oneTimeNeedsADate() {
        assertRejected(draft("Odd", 10.0, Direction.DEBIT, Recurrences.oneTime(null)));
    }

    @Test
    @DisplayName("An unnamed item is not draftable")
    void nameIsRequired() {
        assertRejected(draft("   ", 10.0, Direction.DEBIT, Recurrences.daily()));
    }

    // ---------------------------------------------------------------- helpers

    private void assertRejected(ItemDraft draft) {
        DomainValidationException thrown =
            assertThrows(DomainValidationException.class, () -> translator.toItemRequest(draft, USER));
        assertTrue(thrown.getMessage() != null && !thrown.getMessage().isBlank(),
            "the message is read by the model so it can correct itself; it must say something");
    }

    private static OnMonthDay pair(int month, int day) {
        return new OnMonthDay(month, day);
    }

    private static ItemDraft draft(String name, Double amount, Direction direction, ItemDraft shape) {
        return new ItemDraft(name, amount, direction, shape.recurrence(), shape.onDate(), shape.onWeekday(),
                             shape.weekOfMonth(), shape.dayOfMonth(), shape.secondDayOfMonth(),
                             shape.onMonthDays(), shape.beginDate(), shape.endDate());
    }

    /**
     * Partial drafts carrying only the recurrence and its own fields, so each test above
     * reads as the sentence a user would say rather than a twelve-argument constructor.
     */
    private static final class Recurrences {

        private static ItemDraft of(com.financialplanner.moduleapi.dtos.assistant.Recurrence recurrence,
                                    LocalDate onDate, DayOfWeek onWeekday, Integer weekOfMonth,
                                    Integer dayOfMonth, Integer secondDayOfMonth, List<OnMonthDay> monthDays) {
            return new ItemDraft(null, null, null, recurrence, onDate, onWeekday, weekOfMonth,
                                 dayOfMonth, secondDayOfMonth, monthDays, null, null);
        }

        static ItemDraft daily() {
            return of(com.financialplanner.moduleapi.dtos.assistant.Recurrence.DAILY,
                      null, null, null, null, null, null);
        }

        static ItemDraft oneTime(LocalDate when) {
            return of(com.financialplanner.moduleapi.dtos.assistant.Recurrence.ONE_TIME,
                      when, null, null, null, null, null);
        }

        static ItemDraft weekly(DayOfWeek day) {
            return of(com.financialplanner.moduleapi.dtos.assistant.Recurrence.WEEKLY,
                      null, day, null, null, null, null);
        }

        static ItemDraft everyTwoWeeks(DayOfWeek day) {
            return of(com.financialplanner.moduleapi.dtos.assistant.Recurrence.EVERY_TWO_WEEKS,
                      null, day, null, null, null, null);
        }

        static ItemDraft nthWeekday(DayOfWeek day, Integer weekOfMonth) {
            return of(com.financialplanner.moduleapi.dtos.assistant.Recurrence.NTH_WEEKDAY,
                      null, day, weekOfMonth, null, null, null);
        }

        static ItemDraft monthly(Integer dayOfMonth) {
            return of(com.financialplanner.moduleapi.dtos.assistant.Recurrence.MONTHLY,
                      null, null, null, dayOfMonth, null, null);
        }

        static ItemDraft biMonthly(Integer first, Integer second) {
            return of(com.financialplanner.moduleapi.dtos.assistant.Recurrence.BI_MONTHLY,
                      null, null, null, first, second, null);
        }

        static ItemDraft quarterly(OnMonthDay... pairs) {
            return of(com.financialplanner.moduleapi.dtos.assistant.Recurrence.QUARTERLY,
                      null, null, null, null, null, List.of(pairs));
        }

        static ItemDraft semiAnnual(OnMonthDay... pairs) {
            return of(com.financialplanner.moduleapi.dtos.assistant.Recurrence.SEMI_ANNUAL,
                      null, null, null, null, null, List.of(pairs));
        }

        static ItemDraft annual(OnMonthDay... pairs) {
            return of(com.financialplanner.moduleapi.dtos.assistant.Recurrence.ANNUAL,
                      null, null, null, null, null, List.of(pairs));
        }
    }
}
