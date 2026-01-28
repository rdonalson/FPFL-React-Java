package com.financialplanner.moduleapi.dtos.item;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemType;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.TimePeriod;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a response for an item entity within the financial planner system.
 * This record is utilized for transferring data related to an item from the backend
 * to the client application in a read-only manner.
 * Contains fields that identify the item and its associated properties and attributes,
 * including identifiers, names, types, financial amounts, and time-period-related details.
 * Fields:
 * - id: The unique identifier for the item entity.
 * - userId: The unique user identifier associated with the item.
 * - name: The name or title of the item.
 * - amount: The financial value associated with the item.
 * - ItemType: The type of the item, represented as an instance of the ItemType class.
 * - TimePeriod: The time period linked to the item, represented as an instance of the TimePeriod class.
 * - beginDate: The start date of the item's applicable period.
 * - endDate: The end date of the item's applicable period, if applicable.
 * - weeklyDow: The day of the week relevant to weekly recurrence schedules.
 * - everyOtherWeekDow: The day of the week applicable to every-other-week recurrence schedules.
 * - biMonthlyDay1: The first day of the month relevant to bi-monthly schedules.
 * - biMonthlyDay2: The second day of the month relevant to bi-monthly schedules.
 * - monthlyDom: The day of the month relevant to monthly recurrence schedules.
 * - quarterly1Month, quarterly1Day: The month and day relevant to the first quarter schedule.
 * - quarterly2Month, quarterly2Day: The month and day relevant to the second quarter schedule.
 * - quarterly3Month, quarterly3Day: The month and day relevant to the third quarter schedule.
 * - quarterly4Month, quarterly4Day: The month and day relevant to the fourth quarter schedule.
 * - semiAnnual1Month, semiAnnual1Day: The month and day relevant to the first semi-annual interval.
 * - semiAnnual2Month, semiAnnual2Day: The month and day relevant to the second semi-annual interval.
 * - annualMoy: The month of the year relevant to annual recurrence schedules.
 * - annualDom: The day of the month relevant to annual recurrence schedules.
 * - dateRangeReq: Indicates whether specifying a date range is mandatory for the item.
 * This class is immutable and threadsafe, leveraging the Java record feature for defining
 * its structure and enforcing immutability at the language level. It simplifies the
 * representation of the item response DTO while ensuring consistency and integrity of its data.
 */
public record ItemResponse(Long id, UUID userId, String name, Double amount, ItemType ItemType, TimePeriod TimePeriod,
                           LocalDate beginDate, LocalDate endDate, Integer weeklyDow, Integer everyOtherWeekDow,
                           Integer biMonthlyDay1, Integer biMonthlyDay2, Integer monthlyDom, Integer quarterly1Month,
                           Integer quarterly1Day, Integer quarterly2Month, Integer quarterly2Day,
                           Integer quarterly3Month, Integer quarterly3Day, Integer quarterly4Month,
                           Integer quarterly4Day, Integer semiAnnual1Month, Integer semiAnnual1Day,
                           Integer semiAnnual2Month, Integer semiAnnual2Day, Integer annualMoy, Integer annualDom,
                           Boolean dateRangeReq
) {}
