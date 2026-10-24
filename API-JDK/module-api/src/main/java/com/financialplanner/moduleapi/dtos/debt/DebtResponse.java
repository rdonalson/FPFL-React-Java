package com.financialplanner.moduleapi.dtos.debt;

import com.financialplanner.modulecommonbc.sanitizer.annotations.LenientText;

import java.time.LocalDate;
import java.util.List;

/**
 * A tracked debt with its debit's name, payment and schedule, and its allocations.
 *
 * @param itemId  the debit being paid down
 * @param payment the debit's own scheduled payment (it pays the debt down too)
 * @param period  the debit's time period name
 */
public record DebtResponse(Long id, Long itemId, @LenientText String name, Double payment, @LenientText String period,
                           Double balance, Double apr, LocalDate balanceDate, List<DebtAllocationResponse> allocations) {

    /** {@code perOccurrence} is how much each occurrence of the credit sends (its amount × percent). */
    public record DebtAllocationResponse(Long id, Long creditItemId, @LenientText String creditName,
                                         Double creditAmount, @LenientText String creditPeriod, Double percent,
                                         Double perOccurrence) {}
}
