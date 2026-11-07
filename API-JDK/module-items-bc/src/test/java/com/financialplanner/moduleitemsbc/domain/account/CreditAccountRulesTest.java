package com.financialplanner.moduleitemsbc.domain.account;

import com.financialplanner.modulecommonbc.exception.DomainValidationException;
import com.financialplanner.modulecommonbc.exception.DuplicateItemException;
import com.financialplanner.moduleitemsbc.domain.account.CreditAccountDraft.PaymentDraft;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Port of API-NET's CreditAccountRulesTests: the pure Cards & Loans rules. */
class CreditAccountRulesTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 5);

    private static Item item(long id, String name, double amount, long type) {
        Item item = new Item();
        item.setId(id);
        item.setName(name);
        item.setAmount(amount);
        item.setItemType(new ItemType(type, type == 1 ? "Credit" : "Debit"));
        return item;
    }

    private final Map<Long, Item> items = Map.of(10L, item(10, "Visa payment", 200, 2),
                                                 20L, item(20, "Paycheck", 2000, 1));

    private static BigDecimal m(String value) {
        return value == null ? null : new BigDecimal(value);
    }

    private static CreditAccountDraft draft(String name, PaymentDraft... payments) {
        return new CreditAccountDraft(name, "creditCard", m("1000"), null, null, null, List.of(payments));
    }

    private static PaymentDraft payment(long itemId, String amount, String percent) {
        return new PaymentDraft(itemId, m(amount), m(percent));
    }

    @Test
    void defaultsAndRounding() {
        CreditAccountRules.Normalized clean =
            CreditAccountRules.normalize(draft(" Visa ", payment(20, null, "33.333")), items, List.of(), TODAY);

        assertEquals("Visa", clean.name());
        assertEquals(AccountKind.CreditCard, clean.kind());
        assertEquals(TODAY, clean.balanceDate());
        assertEquals(1, clean.payments().size());
        assertEquals(0, m("33.33").compareTo(clean.payments().getFirst().percent()));
    }

    @Test
    void aDebitPaysItsWholeAmount() {
        CreditAccountRules.Normalized clean =
            CreditAccountRules.normalize(draft("Visa", payment(10, null, null)), items, List.of(), TODAY);
        assertEquals(0, m("100").compareTo(clean.payments().getFirst().percent()));

        assertThrows(DomainValidationException.class, () ->
            CreditAccountRules.normalize(draft("Visa", payment(10, "50", null)), items, List.of(), TODAY));
    }

    @ParameterizedTest
    @CsvSource(value = {"20,NULL,NULL", "20,100,10", "20,2500,NULL", "99,100,NULL"}, nullValues = "NULL")
    void aCreditNeedsOneSensibleAmountOrPercentage(long itemId, String amount, String percent) {
        CreditAccountDraft draft = draft("Visa", payment(itemId, amount, percent));
        assertThrows(DomainValidationException.class,
                     () -> CreditAccountRules.normalize(draft, items, List.of(), TODAY));
    }

    @Test
    void aCreditCantSendMoreThan100PercentAcrossAccountsAndNamesAreUnique() {
        CreditAccount other = new CreditAccount();
        other.setId(2L);
        other.setName("Car Loan");
        AccountPayment taken = new AccountPayment();
        taken.setAccount(other);
        taken.setCreditItem(items.get(20L));
        taken.setPercent(m("80"));
        other.getPayments().add(taken);

        assertThrows(DomainValidationException.class, () ->
            CreditAccountRules.normalize(draft("Visa", payment(20, null, "30")), items, List.of(other), TODAY));
        assertThrows(DuplicateItemException.class, () ->
            CreditAccountRules.normalize(draft("car loan"), items, List.of(other), TODAY));
    }

    @Test
    void onlyACardHasACreditLimit() {
        CreditAccountDraft loan = new CreditAccountDraft("Car", "loan", m("5000"), null, null, m("1000"), List.of());
        assertThrows(DomainValidationException.class,
                     () -> CreditAccountRules.normalize(loan, items, List.of(), TODAY));
    }

    @Test
    void purchasesAreOneTimeDailyOrWeekly() {
        AccountPurchase weekly = new AccountPurchase();
        long period = PurchaseRules.normalize(new PurchaseDraft("Gas", m("40"), 3L, null, null, 5, false), weekly);
        assertEquals(3L, period);
        assertEquals(5, weekly.getWeeklyDow());

        assertThrows(DomainValidationException.class, () ->
            PurchaseRules.normalize(new PurchaseDraft("Rent", m("900"), 6L, null, null, null, false),
                                    new AccountPurchase()));
        assertThrows(DomainValidationException.class, () ->
            PurchaseRules.normalize(new PurchaseDraft("TV", m("500"), 1L, null, null, null, false),
                                    new AccountPurchase()));
    }
}
