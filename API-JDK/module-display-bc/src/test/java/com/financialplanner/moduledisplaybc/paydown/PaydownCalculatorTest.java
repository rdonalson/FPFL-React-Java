package com.financialplanner.moduledisplaybc.paydown;

import com.financialplanner.moduledisplaybc.paydown.PaydownModels.*;
import com.financialplanner.moduledisplaybc.recurrence.RecurrenceOccurrences;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Port of API-NET's PaydownCalculatorTests. Calendar anchors: 2026-10-02 is a Friday, so an every-two-weeks
 * Friday paycheck from then lands on Oct 2, 16, 30, Nov 13, 27, Dec 11, 25. Balances are entered as of Oct 17.
 */
class PaydownCalculatorTest {

    private static final UUID USER = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final LocalDate AS_OF = d(10, 17);
    private static final LocalDate Q_START = d(10, 18);
    private static final LocalDate Q_END = d(12, 31);

    private final PaydownCalculator calculator = new PaydownCalculator(new RecurrenceOccurrences());

    private static LocalDate d(int month, int day) {
        return LocalDate.of(2026, month, day);
    }

    private static BigDecimal m(String value) {
        return new BigDecimal(value);
    }

    private static void assertMoney(String expected, BigDecimal actual) {
        assertEquals(0, m(expected).compareTo(actual), () -> "expected " + expected + " but was " + actual);
    }

    private static Item card(double payment, int dom) {
        Item item = new Item();
        item.setId(10L);
        item.setUserId(USER);
        item.setName("Chase Visa");
        item.setAmount(payment);
        item.setItemType(new ItemType(2L, "Debit"));
        item.setTimePeriod(new TimePeriod(6L, "Monthly"));
        item.setMonthlyDom(dom);
        return item;
    }

    private static Item card() {
        return card(200, 15);
    }

    private static Item paycheck() {
        Item item = new Item();
        item.setId(20L);
        item.setUserId(USER);
        item.setName("Paycheck");
        item.setAmount(2000.0);
        item.setItemType(new ItemType(1L, "Credit"));
        item.setTimePeriod(new TimePeriod(4L, "Every Two Weeks"));
        item.setEveryOtherWeekDow(5);
        item.setBeginDate(d(10, 2));
        return item;
    }

    /** allocations: pairs of (credit item, percent). */
    private static Debt debt(String balance, String apr, Item debit, Object... allocations) {
        Debt debt = new Debt();
        debt.setId(1L);
        debt.setUserId(USER);
        debt.setItem(debit);
        debt.setBalance(m(balance));
        debt.setApr(m(apr));
        debt.setBalanceDate(AS_OF);
        for (int i = 0; i < allocations.length; i += 2) {
            DebtAllocation a = new DebtAllocation();
            a.setId((long) i + 1);
            a.setDebt(debt);
            a.setCreditItem((Item) allocations[i]);
            a.setPercent(m((String) allocations[i + 1]));
            debt.getAllocations().add(a);
        }
        return debt;
    }

    private PaydownResult run(Debt debt, LocalDate start, LocalDate end, Item... items) {
        return calculator.calculate(List.of(items), List.of(debt), start, end);
    }

    @Test
    void allocationsPayTheDebtOff() {
        Item card = card();
        Item paycheck = paycheck();
        PaydownResult result = run(debt("1000", "0", card, paycheck, "25"), Q_START, Q_END, card, paycheck);
        assertEquals(1, result.debts().size());
        DebtPaydown debt = result.debts().getFirst();

        assertEquals("Chase Visa", debt.name());
        assertEquals(List.of(d(10, 30), d(11, 13)), debt.events().stream().map(PaydownEvent::date).toList());
        debt.events().forEach(e -> assertEquals(EventKind.ALLOCATION, e.kind()));
        assertMoney("-500", debt.events().get(0).amount());
        assertMoney("-500", debt.events().get(1).amount());
        assertMoney("500", debt.events().get(0).balanceAfter());
        assertMoney("0", debt.events().get(1).balanceAfter());
        assertEquals(d(11, 13), debt.payoffDate());
        assertMoney("1000", debt.totalPaid());

        // One balance per day; the card's Nov 15 payment never happens because it's already paid off.
        assertEquals(75, debt.balances().size());
        assertMoney("1000", debt.startingBalance());
        assertMoney("1000", debt.balances().get(0));
        assertMoney("500", debt.balances().get(12)); // Oct 30
        assertMoney("0", debt.endingBalance());
    }

    @Test
    void allocationsBecomeExtraDebitsForTheMainLedger() {
        Item card = card();
        Item paycheck = paycheck();
        PaydownResult result = run(debt("1000", "0", card, paycheck, "25"), Q_START, Q_END, card, paycheck);

        assertEquals(2, result.adjustments().size());
        for (LedgerAdjustment a : result.adjustments()) {
            assertEquals("Chase Visa paydown", a.name());
            assertEquals("25% of Paycheck", a.detail());
            assertEquals(10L, a.itemId());
            assertMoney("-500", a.amount());
        }
    }

    @Test
    void theLastPaymentIsCutToWhatIsLeft() {
        Item card = card(0, 15);
        Item paycheck = paycheck();
        PaydownResult result = run(debt("650", "0", card, paycheck, "12.5"), Q_START, Q_END, card, paycheck);
        DebtPaydown debt = result.debts().getFirst();

        // 12.5% of $2,000 = $250: Oct 30 → 400, Nov 13 → 150, Nov 27 pays only 150.
        assertEquals(3, debt.events().size());
        assertMoney("-250", debt.events().get(0).amount());
        assertMoney("-250", debt.events().get(1).amount());
        assertMoney("-150", debt.events().get(2).amount());
        assertEquals(d(11, 27), debt.payoffDate());
        assertEquals("12.5% of Paycheck", result.adjustments().getFirst().detail());
        assertMoney("-150", result.adjustments().getLast().amount());
    }

    @Test
    void theDebitsOwnPaymentPaysItDownAndProjectsPastTheWindow() {
        Item card = card(200, 15);
        PaydownResult result = run(debt("3000", "0", card), Q_START, Q_END, card);
        DebtPaydown debt = result.debts().getFirst();

        assertEquals(List.of(d(11, 15), d(12, 15)), debt.events().stream().map(PaydownEvent::date).toList());
        debt.events().forEach(e -> assertEquals(EventKind.PAYMENT, e.kind()));
        assertMoney("2600", debt.endingBalance());

        // 15 payments of $200 starting Nov 15, 2026 → the last on Jan 15, 2028.
        assertEquals(LocalDate.of(2028, 1, 15), debt.payoffDate());

        // The card's payment is already a debit in the main ledger, so it isn't added again.
        assertTrue(result.adjustments().isEmpty());
    }

    @Test
    void interestIsChargedMonthlyAtMonthEnd() {
        Item card = card(0, 15);
        DebtPaydown debt = run(debt("1200", "12", card), Q_START, Q_END, card).debts().getFirst();

        // 1% a month: Oct 31 +12.00, Nov 30 +12.12, Dec 31 +12.24.
        assertEquals(3, debt.events().size());
        assertMoney("12", debt.events().get(0).amount());
        assertMoney("12.12", debt.events().get(1).amount());
        assertMoney("12.24", debt.events().get(2).amount());
        debt.events().forEach(e -> assertEquals(EventKind.INTEREST, e.kind()));
        assertMoney("1236.36", debt.endingBalance());
        assertMoney("36.36", debt.totalInterest());
        assertNull(debt.payoffDate()); // nothing is paying it
    }

    @Test
    void theDebitsPaymentComesBeforeAllocationsOnTheSameDay() {
        Item card = card(100, 30);
        Item paycheck = paycheck();
        DebtPaydown debt = run(debt("1000", "0", card, paycheck, "25"), Q_START, Q_END, card, paycheck).debts().getFirst();

        List<PaydownEvent> oct30 = debt.events().stream().filter(e -> e.date().equals(d(10, 30))).toList();
        assertEquals(List.of(EventKind.PAYMENT, EventKind.ALLOCATION), oct30.stream().map(PaydownEvent::kind).toList());
        assertMoney("900", oct30.get(0).balanceAfter());
        assertMoney("400", oct30.get(1).balanceAfter());
    }

    @Test
    void aLaterWindowStartsFromTheBalanceAtThatPoint() {
        Item card = card();
        Item paycheck = paycheck();
        PaydownResult result = run(debt("1000", "0", card, paycheck, "25"), d(11, 1), d(11, 30), card, paycheck);
        DebtPaydown debt = result.debts().getFirst();

        assertMoney("500", debt.startingBalance());
        assertMoney("500", debt.balances().get(0));
        assertMoney("0", debt.balances().get(12)); // Nov 13
        assertEquals(1, debt.events().size());
        assertEquals(1, result.adjustments().size());
    }

    @Test
    void daysBeforeTheBalanceDateShowTheEnteredBalance() {
        Item card = card();
        Item paycheck = paycheck();
        DebtPaydown debt = run(debt("1000", "0", card, paycheck, "25"), d(10, 10), d(10, 31), card, paycheck)
            .debts().getFirst();

        assertEquals(22, debt.balances().size());
        debt.balances().subList(0, 8).forEach(b -> assertMoney("1000", b)); // Oct 10–17
        assertMoney("500", debt.balances().get(20)); // Oct 30
        assertMoney("1000", debt.startingBalance());
        assertEquals(1, debt.events().size()); // the Oct 16 paycheck is before the balance date
    }

    @Test
    void aZeroBalanceIsAlreadyPaidOff() {
        Item card = card();
        Item paycheck = paycheck();
        DebtPaydown debt = run(debt("0", "0", card, paycheck, "25"), Q_START, Q_END, card, paycheck).debts().getFirst();

        assertEquals(AS_OF, debt.payoffDate());
        assertTrue(debt.events().isEmpty());
        debt.balances().forEach(b -> assertMoney("0", b));
    }

    @Test
    void debtsWhoseDebitIsGoneOrCreditsThatAreMissingAreSkipped() {
        Item card = card(0, 15);
        Item paycheck = paycheck();
        assertTrue(run(debt("1000", "0", card), Q_START, Q_END, paycheck).debts().isEmpty());

        Item missing = paycheck();
        missing.setId(99L);
        DebtPaydown debt = run(debt("1000", "0", card, missing, "50"), Q_START, Q_END, card).debts().getFirst();
        assertTrue(debt.events().isEmpty());
    }

    @Test
    void everyTwoWeeksPaydaysDontShiftWithTheWindowStart() {
        Item paycheck = paycheck();
        RecurrenceOccurrences occurrences = new RecurrenceOccurrences();

        // Asked from Oct 18 (a Sunday) or Oct 24 (a Saturday), the paycheck still lands on Oct 30.
        assertEquals(d(10, 30), occurrences.of(paycheck, d(10, 18), d(11, 30)).getFirst());
        assertEquals(d(10, 30), occurrences.of(paycheck, d(10, 24), d(11, 30)).getFirst());
        assertEquals(List.of(d(10, 2), d(10, 16), d(10, 30)), occurrences.of(paycheck, d(9, 1), d(10, 31)));
    }
}
