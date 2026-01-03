package com.financialplanner.moduleitemsbc.domain.model;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a data transfer object (DTO) for an item with various attributes
 * including identification, user association, financial details, and date-related
 * recurrence information.
 * This class provides fields and methods to encapsulate the following:
 * - Unique identification for the item and associated user
 * - Descriptive details, such as name and monetary amount
 * - Recurrence patterns spanning weekly, bi-monthly, monthly, quarterly, semi-annual,
 * and annual intervals
 * - Date range with optional requirement for validation
 * It supports field-level validation for non-nullable attributes to ensure data
 * consistency and correctness when used in applications.
 */
@lombok.Data
public class Item {
    @NotNull
    private Integer Id;
    @NotNull
    private UUID UserId;
    @NotNull
    private String Name;
    private Double Amount;
    private Integer FkItemType;
    private Integer FkPeriod;
    private LocalDate BeginDate;
    private LocalDate EndDate;
    private Integer WeeklyDow;
    private Integer EverOtherWeekDow;
    private Integer BiMonthlyDay1;
    private Integer BiMonthlyDay2;
    private Integer MonthlyDom;
    private Integer Quarterly1Month;
    private Integer Quarterly1Day;
    private Integer Quarterly2Month;
    private Integer Quarterly2Day;
    private Integer Quarterly3Month;
    private Integer Quarterly3Day;
    private Integer Quarterly4Month;
    private Integer Quarterly4Day;
    private Integer SemiAnnual1Month;
    private Integer SemiAnnual1Day;
    private Integer SemiAnnual2Month;
    private Integer SemiAnnual2Day;
    private Integer AnnualMoy;
    private Integer AnnualDom;
    @NotNull
    private Boolean DateRangeReq = false;

    /**
     * Default constructor for the ItemEntity class.
     * Initializes a new instance of the ItemEntity class with default or uninitialized property values.
     */
    public Item() {
    }

    /**
     * Constructs an instance of the ItemDto class with the specified parameters.
     *
     * @param id               the identifier of the item
     * @param userId           the unique identifier of the user associated with the item
     * @param name             the name of the item
     * @param amount           the monetary value associated with the item
     * @param fkItemType       the type of the item, represented by a foreign key
     * @param fkPeriod         the period associated with the item, represented by a foreign key
     * @param beginDate        the start date of the item's validity or activity
     * @param endDate          the end date of the item's validity or activity
     * @param weeklyDow        the day of the week for weekly recurrence (1 = Monday, 7 = Sunday)
     * @param everOtherWeekDow the day of the week for every other weekly recurrence
     * @param biMonthlyDay1    the first day of the bi-monthly recurrence
     * @param biMonthlyDay2    the second day of the bi-monthly recurrence
     * @param monthlyDom       the day of the month for the monthly recurrence
     * @param quarterly1Month  the month of the first quarterly recurrence
     * @param quarterly1Day    the day of the first quarterly recurrence
     * @param quarterly2Month  the month of the second quarterly recurrence
     * @param quarterly2Day    the day of the second quarterly recurrence
     * @param quarterly3Month  the month of the third quarterly recurrence
     * @param quarterly3Day    the day of the third quarterly recurrence
     * @param quarterly4Month  the month of the fourth quarterly recurrence
     * @param quarterly4Day    the day of the fourth quarterly recurrence
     * @param semiAnnual1Month the month of the first semi-annual recurrence
     * @param semiAnnual1Day   the day of the first semi-annual recurrence
     * @param semiAnnual2Month the month of the second semi-annual recurrence
     * @param semiAnnual2Day   the day of the second semi-annual recurrence
     * @param annualMoy        the month of the year for annual recurrence
     * @param annualDom        the day of the month for annual recurrence
     * @param dateRangeReq     indicates whether the date range is required for the item
     */
    public Item(Integer id,
                UUID userId,
                String name,
                Double amount,
                Integer fkItemType,
                Integer fkPeriod,
                LocalDate beginDate,
                LocalDate endDate,
                Integer weeklyDow,
                Integer everOtherWeekDow,
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
        Id               = id;
        UserId           = userId;
        Name             = name;
        Amount           = amount;
        FkItemType       = fkItemType;
        FkPeriod         = fkPeriod;
        BeginDate        = beginDate;
        EndDate          = endDate;
        WeeklyDow        = weeklyDow;
        EverOtherWeekDow = everOtherWeekDow;
        BiMonthlyDay1    = biMonthlyDay1;
        BiMonthlyDay2    = biMonthlyDay2;
        MonthlyDom       = monthlyDom;
        Quarterly1Month  = quarterly1Month;
        Quarterly1Day    = quarterly1Day;
        Quarterly2Month  = quarterly2Month;
        Quarterly2Day    = quarterly2Day;
        Quarterly3Month  = quarterly3Month;
        Quarterly3Day    = quarterly3Day;
        Quarterly4Month  = quarterly4Month;
        Quarterly4Day    = quarterly4Day;
        SemiAnnual1Month = semiAnnual1Month;
        SemiAnnual1Day   = semiAnnual1Day;
        SemiAnnual2Month = semiAnnual2Month;
        SemiAnnual2Day   = semiAnnual2Day;
        AnnualMoy        = annualMoy;
        AnnualDom        = annualDom;
        DateRangeReq     = dateRangeReq;
    }
}
