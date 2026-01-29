package com.financialplanner.moduleitemsbc.infrastructure.persistence.entity;

import com.financialplanner.modulecommonbc.converters.BooleanToBitConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents an item entity with associated details including user information,
 * monetary amount, item type, time periods, and various date or recurrence configurations.
 * This entity is mapped to the "items" table in the "fpfl" database schema.
 * It includes various attributes and relationships to define the characteristics
 * and scheduling of an item.
 * Key characteristics include:
 * - User association identified by a UUID.
 * - Attributes for tracking monetary amounts, names, and type relationships.
 * - Date and recurrence configurations for diverse time periods.
 * - Boolean flag to indicate if a date range is required.
 * Entity relationships:
 * - Many-to-one relationship with the {@link ItemType} entity.
 * - Many-to-one relationship with the {@link TimePeriod} entity.
 */
@lombok.Data
@Entity
@Table(name = "items", schema = "fpfl")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long Id;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID UserId;

    @Column(name = "name")
    private String Name;

    @Column(name = "amount")
    private Double Amount;

    /**
     * Represents the type of the item in a many-to-one relationship.
     * This field is mapped to the foreign key column "fk_item_type" in the database
     * and defines a mandatory association with the ItemType entity.
     * The association is lazily fetched, meaning the data is loaded only when accessed.
     */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "fk_item_type", nullable = false)
    private ItemType ItemType;

    /**
     * Represents the time period associated with an entity.
     * The relationship is established as a many-to-one association.
     * The fetch type is configured as LAZY, meaning the data will be
     * loaded on-demand when accessed.
     * This variable is mapped to the database column "fk_time_period"
     * using the @JoinColumn annotation.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fk_time_period")
    private TimePeriod TimePeriod;

    @Column(name = "begin_date")
    private LocalDate BeginDate;

    @Column(name = "end_date")
    private LocalDate EndDate;

    @Column(name = "weekly_dow")
    private Integer WeeklyDow;

    @Column(name = "every_other_week_dow")
    private Integer EveryOtherWeekDow;

    @Column(name = "bi_monthly_day_1")
    private Integer BiMonthlyDay1;

    @Column(name = "bi_monthly_day_2")
    private Integer BiMonthlyDay2;

    @Column(name = "monthly_dom")
    private Integer MonthlyDom;

    @Column(name = "quarterly_1_month")
    private Integer Quarterly1Month;

    @Column(name = "quarterly_1_day")
    private Integer Quarterly1Day;

    @Column(name = "quarterly_2_month")
    private Integer Quarterly2Month;

    @Column(name = "quarterly_2_day")
    private Integer Quarterly2Day;

    @Column(name = "quarterly_3_month")
    private Integer Quarterly3Month;

    @Column(name = "quarterly_3_day")
    private Integer Quarterly3Day;

    @Column(name = "quarterly_4_month")
    private Integer Quarterly4Month;

    @Column(name = "quarterly_4_day")
    private Integer Quarterly4Day;

    @Column(name = "semi_annual_1_month")
    private Integer SemiAnnual1Month;

    @Column(name = "semi_annual_1_day")
    private Integer SemiAnnual1Day;

    @Column(name = "semi_annual_2_month")
    private Integer SemiAnnual2Month;

    @Column(name = "semi_annual_2_day")
    private Integer SemiAnnual2Day;

    @Column(name = "annual_moy")
    private Integer AnnualMoy;

    @Column(name = "annual_dom")
    private Integer AnnualDom;

    /**
     * Represents a flag indicating whether a date range is required for a specific operation or process.
     * This variable is stored in the database as a bit value, with conversion handled by the
     * BooleanToBitConverter class.
     * The field is mapped to the "date_range_req" column in the database and cannot be null.
     */
    @Convert(converter = BooleanToBitConverter.class)
    @Column(name = "date_range_req", nullable = false)
    private Boolean DateRangeReq = false;

    public Item() {}

    /**
     * Constructs a new Item with the given parameters.
     *
     * @param id               The unique identifier of the item.
     * @param userId           The UUID of the user associated with the item.
     * @param name             The name of the item.
     * @param amount           The monetary amount related to the item.
     * @param itemType         The type of the item.
     * @param timePeriod       The time period for the item.
     * @param beginDate        The start date of the item.
     * @param endDate          The end date of the item.
     * @param weeklyDow        The day of the week for weekly occurrences.
     * @param everOtherWeekDow The day of the week for bi-weekly occurrences.
     * @param biMonthlyDay1    The first day for bi-monthly occurrences.
     * @param biMonthlyDay2    The second day for bi-monthly occurrences.
     * @param monthlyDom       The day of the month for monthly occurrences.
     * @param quarterly1Month  The first month in the first quarter.
     * @param quarterly1Day    The day of the first month in the first quarter.
     * @param quarterly2Month  The first month in the second quarter.
     * @param quarterly2Day    The day of the first month in the second quarter.
     * @param quarterly3Month  The first month in the third quarter.
     * @param quarterly3Day    The day of the first month in the third quarter.
     * @param quarterly4Month  The first month in the fourth quarter.
     * @param quarterly4Day    The day of the first month in the fourth quarter.
     * @param semiAnnual1Month The month of the first semi-annual occurrence.
     * @param semiAnnual1Day   The day of the month for the first semi-annual occurrence.
     * @param semiAnnual2Month The month of the second semi-annual occurrence.
     * @param semiAnnual2Day   The day of the month for the second semi-annual occurrence.
     * @param annualMoy        The month of the year for annual occurrences.
     * @param annualDom        The day of the month for annual occurrences.
     * @param dateRangeReq     Indicates whether a date range is required.
     */
    public Item(Long id, UUID userId, String name, Double amount, ItemType itemType, TimePeriod timePeriod,
                LocalDate beginDate, LocalDate endDate, Integer weeklyDow, Integer everOtherWeekDow,
                Integer biMonthlyDay1, Integer biMonthlyDay2, Integer monthlyDom, Integer quarterly1Month,
                Integer quarterly1Day, Integer quarterly2Month, Integer quarterly2Day, Integer quarterly3Month,
                Integer quarterly3Day, Integer quarterly4Month, Integer quarterly4Day, Integer semiAnnual1Month,
                Integer semiAnnual1Day, Integer semiAnnual2Month, Integer semiAnnual2Day, Integer annualMoy,
                Integer annualDom, Boolean dateRangeReq) {
        Id                = id;
        UserId            = userId;
        Name              = name;
        Amount            = amount;
        ItemType          = itemType;
        TimePeriod        = timePeriod;
        BeginDate         = beginDate;
        EndDate           = endDate;
        WeeklyDow         = weeklyDow;
        EveryOtherWeekDow = everOtherWeekDow;
        BiMonthlyDay1     = biMonthlyDay1;
        BiMonthlyDay2     = biMonthlyDay2;
        MonthlyDom        = monthlyDom;
        Quarterly1Month   = quarterly1Month;
        Quarterly1Day     = quarterly1Day;
        Quarterly2Month   = quarterly2Month;
        Quarterly2Day     = quarterly2Day;
        Quarterly3Month   = quarterly3Month;
        Quarterly3Day     = quarterly3Day;
        Quarterly4Month   = quarterly4Month;
        Quarterly4Day     = quarterly4Day;
        SemiAnnual1Month  = semiAnnual1Month;
        SemiAnnual1Day    = semiAnnual1Day;
        SemiAnnual2Month  = semiAnnual2Month;
        SemiAnnual2Day    = semiAnnual2Day;
        AnnualMoy         = annualMoy;
        AnnualDom         = annualDom;
        DateRangeReq      = dateRangeReq;
    }
}
