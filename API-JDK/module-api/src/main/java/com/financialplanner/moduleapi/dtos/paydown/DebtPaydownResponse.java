package com.financialplanner.moduleapi.dtos.paydown;

import com.financialplanner.modulecommonbc.sanitizer.annotations.LenientText;

import java.time.LocalDate;
import java.util.List;

/**
 * One tracked debt over the requested window ({@code POST /display/paydown}, same body as
 * {@code /display/ledger}). {@code balances} has one value per day of the window; {@code payoffDate} may fall
 * after the window, or be null if the debt isn't paid off within 30 years.
 *
 * @param itemId the debit being paid down
 */
public record DebtPaydownResponse(Long debtId, Long itemId, @LenientText String name, Double apr,
                                  Double enteredBalance, LocalDate balanceDate, Double startingBalance,
                                  Double endingBalance, Double totalPaid, Double totalInterest, LocalDate payoffDate,
                                  List<Double> balances, List<PaydownEventResponse> events) {

    /**
     * {@code kind} is {@code payment} (the debit's own payment), {@code allocation} (a share of a credit) or
     * {@code interest}. Payments and allocations are negative; interest is positive.
     */
    public record PaydownEventResponse(LocalDate date, String kind, Long sourceItemId,
                                       @LenientText String sourceName, Double percent, Double amount,
                                       Double balanceAfter) {}
}
