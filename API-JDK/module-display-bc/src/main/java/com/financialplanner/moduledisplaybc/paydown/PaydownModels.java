package com.financialplanner.moduledisplaybc.paydown;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Debt Paydown's results (same shapes as API-NET's {@code PaydownModels}). */
public final class PaydownModels {

    private PaydownModels() {}

    public enum EventKind {
        /** The debit's own scheduled payment (e.g. the card's monthly payment). */
        PAYMENT,
        /** A percentage of a credit sent to the debt. */
        ALLOCATION,
        /** A month's interest added to the balance. */
        INTEREST
    }

    /**
     * One change to a debt's balance.
     *
     * @param sourceItemId the debit (payments) or credit (allocations) the money came from; null for interest
     * @param percent      allocations only: the share of the credit
     * @param amount       payments and allocations are negative (they reduce what's owed); interest is positive
     */
    public record PaydownEvent(LocalDate date, EventKind kind, Long sourceItemId, String sourceName,
                               BigDecimal percent, BigDecimal amount, BigDecimal balanceAfter) {}

    /**
     * One tracked debt over the ledger's window.
     *
     * @param enteredBalance   what was owed when the user entered it, as of {@code balanceDate}
     * @param startingBalance  owed at the start of the window (before its first day's activity)
     * @param endingBalance    owed at the end of the window
     * @param balances         owed at the end of each day of the window, one value per day
     * @param payoffDate       the day the balance reaches zero, which may be after the window (projected up to
     *                         {@link PaydownCalculator#HORIZON_YEARS} years out); null if it never does
     * @param events           balance changes within the window, in date order
     */
    public record DebtPaydown(Long debtId, Long debtItemId, String name, BigDecimal apr, BigDecimal enteredBalance,
                              LocalDate balanceDate, BigDecimal startingBalance, BigDecimal endingBalance,
                              List<BigDecimal> balances, BigDecimal totalPaid, BigDecimal totalInterest,
                              LocalDate payoffDate, List<PaydownEvent> events) {}

    /**
     * An extra debit for the main ledger: money a credit sent to a debt.
     *
     * @param itemId the debt's debit item
     * @param name   e.g. "Chase Visa paydown"
     * @param detail e.g. "30% of Paycheck"
     * @param amount negative
     */
    public record LedgerAdjustment(LocalDate date, Long itemId, String name, String detail, BigDecimal amount) {}

    public record PaydownResult(List<DebtPaydown> debts, List<LedgerAdjustment> adjustments) {}
}
