package com.financialplanner.moduleapi.dtos.item;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemType;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.TimePeriod;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record ItemResponse(
    Long id,
    UUID userId,
    String name,
    Double amount,
    ItemType ItemType,
    TimePeriod TimePeriod,
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
) {}
