package com.financialplanner.moduleapi.dtos.account;

import com.financialplanner.modulecommonbc.sanitizer.annotations.LenientText;

import java.time.LocalDate;
import java.util.List;

/**
 * One credit card or loan over the requested window ({@code POST /display/accounts}, same body as
 * {@code /display/ledger}). {@code balances} has one value per day of the window; {@code payoffDate} may fall
 * after the window, or be null if the balance isn't cleared within 30 years.
 */
public record AccountLedgerResponse(Long accountId, @LenientText String name, String kind, Double apr,
                                    Double creditLimit, Double openingBalance, LocalDate balanceDate,
                                    Double startingBalance, Double endingBalance, Double totalPurchases,
                                    Double totalPaid, Double totalInterest, LocalDate payoffDate,
                                    List<Double> balances, List<AccountEventResponse> events) {

    /**
     * {@code kind} is {@code purchase}, {@code payment} or {@code interest}. Purchases and interest are positive;
     * payments are negative. {@code sourceId} is the purchase id (purchases) or the credit or debit item id
     * (payments).
     */
    public record AccountEventResponse(LocalDate date, String kind, Long sourceId, @LenientText String sourceName,
                                       Double percent, Double amount, Double balanceAfter) {}
}
