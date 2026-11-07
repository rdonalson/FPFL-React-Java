package com.financialplanner.moduleapi.dtos.account;

import com.financialplanner.modulecommonbc.sanitizer.annotations.LenientText;

import java.time.LocalDate;

/**
 * Body of {@code POST /accounts/{id}/purchases} and {@code PUT /accounts/{id}/purchases/{purchaseId}}.
 * {@code fkPeriod} is 1 (One Time, needs {@code beginDate}), 2 (Daily) or 3 (Weekly, needs {@code weeklyDow}
 * 0–6). With {@code dateRangeReq} a recurring purchase runs only from {@code beginDate} to {@code endDate}.
 */
public record PurchaseRequest(@LenientText String name, Double amount, Long fkPeriod, LocalDate beginDate,
                              LocalDate endDate, Integer weeklyDow, Boolean dateRangeReq) {}
