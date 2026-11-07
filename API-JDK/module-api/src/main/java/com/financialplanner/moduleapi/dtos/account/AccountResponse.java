package com.financialplanner.moduleapi.dtos.account;

import com.financialplanner.modulecommonbc.sanitizer.annotations.LenientText;

import java.time.LocalDate;
import java.util.List;

/** A credit card or loan with its purchases and the items that pay it. {@code kind} is {@code creditCard} or {@code loan}. */
public record AccountResponse(Long id, @LenientText String name, String kind, Double openingBalance,
                              LocalDate balanceDate, Double apr, Double creditLimit,
                              List<PurchaseResponse> purchases, List<AccountPaymentResponse> payments) {

    public record PurchaseResponse(Long id, Long accountId, @LenientText String name, Double amount, Long fkPeriod,
                                   @LenientText String period, LocalDate beginDate, LocalDate endDate,
                                   Integer weeklyDow, Boolean dateRangeReq) {}

    /**
     * {@code itemTypeId} is 1 (credit) or 2 (debit). {@code perOccurrence} is how much each occurrence sends: a
     * credit's fixed amount or its percentage, or a debit's whole amount.
     */
    public record AccountPaymentResponse(Long id, Long itemId, Long itemTypeId, @LenientText String itemName,
                                         Double itemAmount, @LenientText String itemPeriod, Double amount,
                                         Double percent, Double perOccurrence) {}
}
