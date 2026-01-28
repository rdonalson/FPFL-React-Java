package com.financialplanner.moduleapi.dtos.item;

import com.financialplanner.modulecommonbc.exception.DomainValidationException;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record ItemRequest(
    @NotNull UUID userId,
    @NotNull String name,
    @NotNull Double amount,
    @NotNull Integer fkItemType,
    @NotNull Integer fkPeriod,
    LocalDate beginDate,
    LocalDate endDate,
    Integer weeklyDow,
    Integer everyOtherWeekDow,
    Integer biMonthlyDay1,
    Integer biMonthlyDay2,
    Integer monthlyDom,
    Integer quarterly1Month,
    Integer quarterly1Day,
    Integer quarterly2Month,
    Integer quarterly2Day,
    Integer quarterly3Month,
    Integer quarterly3Day,
    Integer quarterly4Month,
    Integer quarterly4Day,
    Integer semiAnnual1Month,
    Integer semiAnnual1Day,
    Integer semiAnnual2Month,
    Integer semiAnnual2Day,
    Integer annualMoy,
    Integer annualDom,
    Boolean dateRangeReq
) {
    public ItemRequest {
//        if (userId == null) {
//            throw new DomainValidationException("Item UserId is required and cannot be blank");
//        }
//        if (name == null || name.isBlank()) {
//            throw new DomainValidationException("Item Name is required and cannot be blank");
//        }
//        if (amount == null || amount <= 0) {
//            throw new DomainValidationException("Item Amount is required and must be a positive currency value");
//        }
//        if (fkItemType == null || fkItemType <= 0) {
//            throw new DomainValidationException("Item ItemType is required and must be a positive integer");
//        }
//        if (fkPeriod == null || fkPeriod <= 0) {
//            throw new DomainValidationException("Item Period is required and must be a positive integer");
//        }
    }
}
