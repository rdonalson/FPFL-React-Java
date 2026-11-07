package com.financialplanner.moduleitemsbc.domain.account;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * What a request asks a purchase to be, before {@link PurchaseRules} checks it.
 *
 * @param timePeriodId 1 (One Time), 2 (Daily) or 3 (Weekly)
 * @param weeklyDow    Weekly only: 0 = Sunday … 6 = Saturday
 */
public record PurchaseDraft(String name, BigDecimal amount, Long timePeriodId, LocalDate beginDate,
                            LocalDate endDate, Integer weeklyDow, boolean dateRangeReq) {}
