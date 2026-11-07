package com.financialplanner.moduleapi.dtos.account;

import com.financialplanner.modulecommonbc.sanitizer.annotations.LenientText;

import java.time.LocalDate;
import java.util.List;

/**
 * Body of {@code POST /accounts} and {@code PUT /accounts/{id}}. {@code kind} is {@code creditCard} (default) or
 * {@code loan}; {@code apr} defaults to 0 and {@code balanceDate} to today; {@code creditLimit} is optional
 * (cards only). {@code payments} (your credits and debits that pay it, at most 20) replaces the account's
 * payments on update; purchases are managed under {@code /accounts/{id}/purchases}. The owner comes from the
 * access token, never the body.
 */
public record AccountRequest(@LenientText String name, String kind, Double openingBalance, LocalDate balanceDate,
                             Double apr, Double creditLimit, List<AccountPaymentRequest> payments) {

    /**
     * One of your items paying the account each time it occurs. A credit sends {@code amount} or
     * {@code percent} (not both). A debit (e.g. the card's monthly payment) pays its whole amount, so send
     * neither.
     */
    public record AccountPaymentRequest(Long itemId, Double amount, Double percent) {}
}
