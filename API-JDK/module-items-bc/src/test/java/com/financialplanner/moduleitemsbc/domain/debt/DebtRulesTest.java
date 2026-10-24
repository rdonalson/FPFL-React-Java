package com.financialplanner.moduleitemsbc.domain.debt;

import com.financialplanner.modulecommonbc.exception.DomainValidationException;
import com.financialplanner.moduleitemsbc.domain.debt.DebtDraft.AllocationDraft;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/** The pure Debt Paydown rules (the service tests cover them end to end; these pin the edges). */
class DebtRulesTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 18);

    private static Item item(long id, String name, long type) {
        Item item = new Item();
        item.setId(id);
        item.setName(name);
        item.setAmount(100.0);
        item.setItemType(new ItemType(type, type == 1 ? "Credit" : "Debit"));
        return item;
    }

    private final Item card = item(10, "Chase Visa", 2);
    private final Item paycheck = item(20, "Paycheck", 1);
    private final Item initialAmount = item(30, "Initial Amount", 3);
    private final Map<Long, Item> owner = Map.of(10L, card, 20L, paycheck, 30L, initialAmount);

    private static DebtDraft draft(String balance, String apr, LocalDate date, AllocationDraft... allocations) {
        return new DebtDraft(10L, balance == null ? null : new BigDecimal(balance),
                             apr == null ? null : new BigDecimal(apr), date, Arrays.asList(allocations));
    }

    private static AllocationDraft alloc(Long creditId, String percent) {
        return new AllocationDraft(creditId, percent == null ? null : new BigDecimal(percent));
    }

    private DebtRules.Normalized normalize(DebtDraft draft, Collection<Debt> others) {
        return DebtRules.normalize(draft, card, owner, others, TODAY);
    }

    private void assertInvalid(String messagePart, Runnable call) {
        DomainValidationException ex = assertThrows(DomainValidationException.class, call::run);
        assertTrue(ex.getMessage().contains(messagePart), ex.getMessage());
    }

    @Test
    void roundsAndDefaults() {
        DebtRules.Normalized n = normalize(draft("99.995", "24.9995", null, alloc(20L, "12.345")), List.of());

        assertEquals(new BigDecimal("100.00"), n.balance());
        assertEquals(new BigDecimal("25.000"), n.apr());
        assertEquals(TODAY, n.balanceDate());
        assertEquals(new BigDecimal("12.35"), n.allocations().getFirst().percent());
        assertSame(paycheck, n.allocations().getFirst().credit());
    }

    @Test
    void keepsAGivenBalanceDateAndAcceptsTheLimits() {
        LocalDate asOf = LocalDate.of(2026, 1, 31);
        DebtRules.Normalized n = normalize(draft("0", "100", asOf, alloc(20L, "100")), List.of());

        assertEquals(asOf, n.balanceDate());
        assertEquals(0, BigDecimal.ZERO.compareTo(n.balance()));
        assertEquals(0, new BigDecimal("100").compareTo(n.allocations().getFirst().percent()));

        assertEquals(0, normalize(draft("1000000000", null, null), List.of()).balance()
                                                                         .compareTo(DebtRules.MAX_BALANCE));
    }

    @Test
    void onlyADebitCanBeTracked() {
        assertInvalid("Only a debit", () -> DebtRules.normalize(draft("1", null, null), paycheck, owner, List.of(), TODAY));
        assertInvalid("Only a debit",
                      () -> DebtRules.normalize(draft("1", null, null), initialAmount, owner, List.of(), TODAY));
    }

    @Test
    void anInitialAmountIsNotACredit() {
        assertInvalid("\"Initial Amount\" is not a credit", () -> normalize(draft("1", null, null, alloc(30L, "10")), List.of()));
    }

    @Test
    void anAllocationWithoutACreditOrPercentIsRejected() {
        assertInvalid("one of your credits", () -> normalize(draft("1", null, null, alloc(null, "10")), List.of()));
        assertInvalid("one of your credits", () -> normalize(draft("1", null, null, (AllocationDraft) null), List.of()));
        assertInvalid("more than 0%", () -> normalize(draft("1", null, null, alloc(20L, null)), List.of()));
    }

    @Test
    void atMostTwentyAllocations() {
        AllocationDraft[] many = new AllocationDraft[DebtRules.MAX_ALLOCATIONS + 1];
        Arrays.fill(many, alloc(20L, "1"));
        assertInvalid("at most 20 allocations", () -> normalize(draft("1", null, null, many), List.of()));
    }

    @Test
    void theCapCountsEveryOtherDebt() {
        Debt a = otherDebt("33.33");
        Debt b = otherDebt("33.33");

        assertInvalid("already 66.66% allocated to other debts; at most 33.34% is left",
                      () -> normalize(draft("1", null, null, alloc(20L, "33.35")), List.of(a, b)));
        assertEquals(1, normalize(draft("1", null, null, alloc(20L, "33.34")), List.of(a, b)).allocations().size());
    }

    @Test
    void formatsPercentagesWithoutTrailingZeros() {
        assertEquals("25", DebtRules.format(new BigDecimal("25.00")));
        assertEquals("12.5", DebtRules.format(new BigDecimal("12.50")));
        assertEquals("100", DebtRules.format(new BigDecimal("100.00")));
        assertEquals("0.25", DebtRules.format(new BigDecimal("0.25")));
    }

    @Test
    void typeOfIsZeroWhenUnknown() {
        assertEquals(0L, DebtRules.typeOf(null));
        assertEquals(0L, DebtRules.typeOf(new Item()));
        assertEquals(2L, DebtRules.typeOf(card));
    }

    private Debt otherDebt(String percent) {
        Debt debt = new Debt();
        DebtAllocation allocation = new DebtAllocation();
        allocation.setDebt(debt);
        allocation.setCreditItem(paycheck);
        allocation.setPercent(new BigDecimal(percent));
        debt.getAllocations().add(allocation);
        return debt;
    }
}
