package com.financialplanner.moduleapi.dtos.item;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a data transfer object used to create or modify an item in the financial planner system.
 * This record encapsulates various properties necessary to define an item, including user association,
 * financial details, and schedule-related attributes.
 * This class enforces immutability and ensures a thread-safe representation of the item request data.
 * It serves as an input model for item-related operations.
 * Fields:
 * - userId: The unique identifier of the user associated with the item (required).
 * - name: The name or title of the item (required).
 * - amount: The monetary value of the item (required, must be positive).
 * - fkItemType: The foreign key identifying the item type (required, positive integer).
 * - fkPeriod: The foreign key identifying the period type associated with the item (required, positive integer).
 * - beginDate: The start date of the applicable period for the item, if required.
 * - endDate: The end date of the applicable period for the item, if required.
 * - weeklyDow: Represents the day of the week for weekly recurrence schedules.
 * - everyOtherWeekDow: Represents the day of the week for every-other-week recurrence schedules.
 * - biMonthlyDay1: Represents the first day of the month for bi-monthly schedules.
 * - biMonthlyDay2: Represents the second day of the month for bi-monthly schedules.
 * - monthlyDom: Represents the day of the month for monthly recurrence schedules.
 * - quarterly1Month: Represents the month for the first quarter schedule.
 * - quarterly1Day: Represents the day for the first quarter schedule.
 * - quarterly2Month: Represents the month for the second quarter schedule.
 * - quarterly2Day: Represents the day for the second quarter schedule.
 * - quarterly3Month: Represents the month for the third quarter schedule.
 * - quarterly3Day: Represents the day for the third quarter schedule.
 * - quarterly4Month: Represents the month for the fourth quarter schedule.
 * - quarterly4Day: Represents the day for the fourth quarter schedule.
 * - semiAnnual1Month: Represents the month for the first semi-annual interval.
 * - semiAnnual1Day: Represents the day for the first semi-annual interval.
 * - semiAnnual2Month: Represents the month for the second semi-annual interval.
 * - semiAnnual2Day: Represents the day for the second semi-annual interval.
 * - annualMoy: Represents the month relevant to annual recurrence schedules.
 * - annualDom: Represents the day of the month relevant to annual recurrence schedules.
 * - dateRangeReq: Indicates whether a specific date range is mandatory for the item.
 */
public record ItemRequest(@NotNull UUID userId, @NotNull String name, @NotNull Double amount,
                          @NotNull Integer fkItemType, @NotNull Integer fkPeriod, LocalDate beginDate,
                          LocalDate endDate, Integer weeklyDow, Integer everyOtherWeekDow, Integer biMonthlyDay1,
                          Integer biMonthlyDay2, Integer monthlyDom, Integer quarterly1Month, Integer quarterly1Day,
                          Integer quarterly2Month, Integer quarterly2Day, Integer quarterly3Month,
                          Integer quarterly3Day, Integer quarterly4Month, Integer quarterly4Day,
                          Integer semiAnnual1Month, Integer semiAnnual1Day, Integer semiAnnual2Month,
                          Integer semiAnnual2Day, Integer annualMoy, Integer annualDom, Boolean dateRangeReq
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
