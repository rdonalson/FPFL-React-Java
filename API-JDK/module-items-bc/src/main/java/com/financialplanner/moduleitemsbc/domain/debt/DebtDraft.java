package com.financialplanner.moduleitemsbc.domain.debt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * What a request asks a tracked debt to be, before {@link DebtRules} checks it.
 *
 * @param itemId      the debit to track (required to create; can't change on update)
 * @param balance     what is owed as of {@code balanceDate}
 * @param apr         annual percentage rate; null means 0
 * @param balanceDate the day {@code balance} was true; null means today
 * @param allocations shares of the owner's credits sent to the debt
 */
public record DebtDraft(Long itemId, BigDecimal balance, BigDecimal apr, LocalDate balanceDate,
                        List<AllocationDraft> allocations) {

    /** A share of one credit: {@code percent} of every occurrence. */
    public record AllocationDraft(Long creditItemId, BigDecimal percent) {}
}
