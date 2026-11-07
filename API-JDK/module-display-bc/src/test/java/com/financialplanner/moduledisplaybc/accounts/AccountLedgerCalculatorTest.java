package com.financialplanner.moduledisplaybc.accounts;

import com.financialplanner.moduledisplaybc.accounts.AccountLedgerModels.*;
import com.financialplanner.moduledisplaybc.paydown.PaydownModels.LedgerAdjustment;
import com.financialplanner.moduledisplaybc.recurrence.RecurrenceOccurrences;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Port of API-NET's AccountLedgerCalculatorTests. Same calendar as {@code PaydownCalculatorTest}: the
 * every-two-weeks Friday paycheck from Oct 2, 2026 lands on Oct 2, 16, 30, Nov 13, 27, Dec 11, 25. Balances are
 * entered as of Oct 17.
 */
class AccountLedgerCalculatorTest {

    private static final UUID USER = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final LocalDate AS_OF = d(10, 17);
    private static final LocalDate Q_START = d(10, 18);
    private static final LocalDate Q_END = d(12, 31);

    private final AccountLedgerCalculator calculator = new AccountLedgerCalculator(new RecurrenceOccurrences());

    private static LocalDate d(int month, int day) {
        return LocalDate.of(2026, month, day);
    }

    private static BigDecimal m(String value) {
        return new BigDecimal(value);
    }

    private static void assertMoney(String expected, BigDecimal actual) {
        assertEquals(0, m(expected).compareTo(actual), () -> "expected " + expected + " but was " + actual);
    }

    private static void assertAmounts(List<String> expected, List<BigDecimal> actual) {
        assertEquals(expected.size(), actual.size(), () -> "amounts " + actual);
        for (int i = 0; i < expected.size(); i++) {
            assertMoney(expected.get(i), actual.get(i));
        }
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

    private static Item cardPayment() {
        Item item = new Item();
        item.setId(10L);
        item.setUserId(USER);
        item.setName("Visa payment");
        item.setAmount(200.0);
        item.setItemType(new ItemType(2L, "Debit"));
        item.setTimePeriod(new TimePeriod(6L, "Monthly"));
        item.setMonthlyDom(15);
        return item;
    }

    private static CreditAccount visa(String balance, String apr) {
        CreditAccount account = new CreditAccount();
        account.setId(1L);
        account.setUserId(USER);
        account.setName("Visa");
        account.setKind(AccountKind.CreditCard);
        account.setOpeningBalance(m(balance));
        account.setBalanceDate(AS_OF);
        account.setApr(m(apr));
        return account;
    }

    private static CreditAccount pay(CreditAccount account, Item source, String amount, String percent) {
        AccountPayment payment = new AccountPayment();
        payment.setId((long) account.getPayments().size() + 1);
        payment.setAccount(account);
        payment.setCreditItem(source);
        payment.setAmount(amount == null ? null : m(amount));
        payment.setPercent(percent == null ? null : m(percent));
        account.getPayments().add(payment);
        return account;
    }

    private AccountLedgerResult run(CreditAccount account, LocalDate start, LocalDate end, Item... items) {
        return calculator.calculate(List.of(items), List.of(account), start, end);
    }

    private static List<BigDecimal> amounts(AccountLedger ledger) {
        return ledger.events().stream().map(AccountEvent::amount).toList();
    }

    @Test
    void aPercentageOfACreditPaysTheAccountOffAndLeavesTheBank() {
        Item paycheck = paycheck();
        AccountLedgerResult result = run(pay(visa("1000", "0"), paycheck, null, "25"), Q_START, Q_END, paycheck);
        assertEquals(1, result.accounts().size());
        AccountLedger account = result.accounts().getFirst();

        assertEquals(List.of(d(10, 30), d(11, 13)), account.events().stream().map(AccountEvent::date).toList());
        assertAmounts(List.of("-500", "-500"), amounts(account));
        assertMoney("0", account.endingBalance());
        assertEquals(d(11, 13), account.payoffDate());
        assertEquals(75, account.balances().size());

        assertEquals(2, result.adjustments().size());
        for (LedgerAdjustment a : result.adjustments()) {
            assertEquals("Visa payment", a.name());
            assertEquals("25% of Paycheck", a.detail());
            assertMoney("-500", a.amount());
        }
    }

    @Test
    void aFixedAmountIsCutToWhatIsOwed() {
        Item paycheck = paycheck();
        AccountLedgerResult result = run(pay(visa("700", "0"), paycheck, "300", null), Q_START, Q_END, paycheck);
        AccountLedger account = result.accounts().getFirst();

        assertAmounts(List.of("-300", "-300", "-100"), amounts(account));
        assertEquals(d(11, 27), account.payoffDate());
        assertAmounts(List.of("-300", "-300", "-100"),
                      result.adjustments().stream().map(LedgerAdjustment::amount).toList());
        assertEquals("from Paycheck", result.adjustments().getFirst().detail());
    }

    @Test
    void purchasesRaiseTheBalanceButNeverTouchTheBank() {
        CreditAccount account = visa("0", "0");
        AccountPurchase groceries = new AccountPurchase();
        groceries.setId(5L);
        groceries.setAccount(account);
        groceries.setName("Groceries");
        groceries.setAmount(m("50"));
        groceries.setTimePeriod(new TimePeriod(3L, "Weekly"));
        groceries.setWeeklyDow(1);
        account.getPurchases().add(groceries);

        AccountLedgerResult result = run(account, d(10, 18), d(10, 31));
        AccountLedger ledger = result.accounts().getFirst();

        // Mondays Oct 19 and 26.
        assertAmounts(List.of("50", "50"), amounts(ledger));
        ledger.events().forEach(e -> assertEquals(AccountEventKind.PURCHASE, e.kind()));
        assertMoney("100", ledger.endingBalance());
        assertMoney("100", ledger.totalPurchases());
        assertNull(ledger.payoffDate()); // purchases every week, nothing pays it
        assertTrue(result.adjustments().isEmpty());
    }

    @Test
    void aDebitPaysItsWholeAmountWithoutASecondBankDeduction() {
        Item card = cardPayment();
        AccountLedgerResult result = run(pay(visa("600", "0"), card, null, "100"), Q_START, Q_END, card);
        AccountLedger ledger = result.accounts().getFirst();

        assertEquals(List.of(d(11, 15), d(12, 15)), ledger.events().stream().map(AccountEvent::date).toList());
        assertMoney("200", ledger.endingBalance());
        assertEquals(LocalDate.of(2027, 1, 15), ledger.payoffDate()); // projected past the window
        assertTrue(result.adjustments().isEmpty()); // the debit is already on the main ledger
    }

    @Test
    void interestIsAddedAtEachMonthEnd() {
        AccountLedger ledger = run(visa("1200", "12"), d(10, 18), d(11, 30)).accounts().getFirst();

        assertEquals(List.of(d(10, 31), d(11, 30)), ledger.events().stream().map(AccountEvent::date).toList());
        assertAmounts(List.of("12", "12.12"), amounts(ledger));
        assertMoney("1224.12", ledger.endingBalance());
        assertNull(ledger.payoffDate());
    }
}
