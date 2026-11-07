package com.financialplanner.moduleitemsbc.domain.account;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * What a request asks a card or loan to be, before {@link CreditAccountRules} checks it.
 *
 * @param kind        {@code creditCard} or {@code loan} (any case); null means a credit card
 * @param apr         null means 0
 * @param balanceDate null means today
 * @param creditLimit optional; cards only
 * @param payments    the owner's credits and debits that pay the account
 */
public record CreditAccountDraft(String name, String kind, BigDecimal openingBalance, LocalDate balanceDate,
                                 BigDecimal apr, BigDecimal creditLimit, List<PaymentDraft> payments) {

    /**
     * One of the owner's items paying the account: for a credit, a fixed {@code amount} or a {@code percent}
     * (not both); for a debit, neither (it pays its whole amount).
     */
    public record PaymentDraft(Long creditItemId, BigDecimal amount, BigDecimal percent) {}
}
