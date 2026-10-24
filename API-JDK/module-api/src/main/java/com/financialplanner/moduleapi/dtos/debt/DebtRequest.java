package com.financialplanner.moduleapi.dtos.debt;

import java.time.LocalDate;
import java.util.List;

/**
 * Body of {@code POST /debts} and {@code PUT /debts/{id}}. {@code itemId} (one of your debits) is required to
 * create and can't change on update. {@code apr} defaults to 0 and {@code balanceDate} to today. The owner
 * comes from the access token, never the body.
 *
 * @param itemId      the debit being paid down
 * @param balance     what is owed as of {@code balanceDate}
 * @param apr         annual percentage rate, 0–100
 * @param balanceDate the day {@code balance} was true (yyyy-MM-dd)
 * @param allocations shares of your credits sent to the debt each time the credit occurs (at most 20)
 */
public record DebtRequest(Long itemId, Double balance, Double apr, LocalDate balanceDate,
                          List<DebtAllocationRequest> allocations) {

    /** {@code percent} (above 0, at most 100) of every occurrence of the credit goes to the debt. */
    public record DebtAllocationRequest(Long creditItemId, Double percent) {}
}
