package com.financialplanner.moduledisplaybc.accounts;

import com.financialplanner.moduledisplaybc.paydown.PaydownModels.LedgerAdjustment;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.AccountKind;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Cards & Loans results (same shapes as API-NET's {@code AccountLedgerModels}). */
public final class AccountLedgerModels {

    private AccountLedgerModels() {}

    public enum AccountEventKind {
        /** A purchase charged to the account (raises the balance). */
        PURCHASE,
        /** Money a credit or debit sent to the account (lowers the balance, leaves the bank). */
        PAYMENT,
        /** A month's interest added to the balance. */
        INTEREST
    }

    /**
     * One change to an account's balance.
     *
     * @param sourceId   the purchase (purchases) or the credit or debit item (payments); null for interest
     * @param percent    payments only: the share of the credit, when the payment is a percentage
     * @param amount     purchases and interest are positive (they raise what's owed); payments are negative
     */
    public record AccountEvent(LocalDate date, AccountEventKind kind, Long sourceId, String sourceName,
                               BigDecimal percent, BigDecimal amount, BigDecimal balanceAfter) {}

    /**
     * One card or loan over the ledger's window.
     *
     * @param startingBalance owed at the start of the window (before its first day's activity)
     * @param endingBalance   owed at the end of the window
     * @param balances        owed at the end of each day of the window, one value per day
     * @param totalPurchases  within the window, positive
     * @param totalPaid       within the window, positive
     * @param payoffDate      the day the balance reaches zero and stays there, which may be after the window
     *                        (projected up to {@link AccountLedgerCalculator#HORIZON_YEARS} years out); null if it
     *                        never does in that time
     * @param events          balance changes within the window, in date order
     */
    public record AccountLedger(Long accountId, String name, AccountKind kind, BigDecimal apr, BigDecimal creditLimit,
                                BigDecimal openingBalance, LocalDate balanceDate, BigDecimal startingBalance,
                                BigDecimal endingBalance, List<BigDecimal> balances, BigDecimal totalPurchases,
                                BigDecimal totalPaid, BigDecimal totalInterest, LocalDate payoffDate,
                                List<AccountEvent> events) {}

    /** Every account, plus the money credits sent to them as extra debits for the main ledger. */
    public record AccountLedgerResult(List<AccountLedger> accounts, List<LedgerAdjustment> adjustments) {}
}
