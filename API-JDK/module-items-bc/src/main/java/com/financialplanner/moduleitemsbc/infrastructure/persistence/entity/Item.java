package com.financialplanner.moduleitemsbc.infrastructure.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents an Item entity which models the details of an item with various
 * scheduling and relational attributes such as type, time period, and
 * occurrence schedules. This entity is mapped to the "items" table within
 * the "fpfl" schema in the database.
 * Each instance of this class corresponds to a row in the "items" table.
 * The primary key is represented by the {@code id} field.
 * The entity tracks various attributes, including the owner's unique identifier
 * (UUID), scheduling information (weekly, monthly, quarterly, semi-annual, annual),
 * date range settings, and relationships to associated item types and time periods.
 * The following fields support specific business logic:
 * - {@code dateRangeReq}: Indicates whether a specific date range is required for the item.
 * Relational mappings include:
 * - {@code ItemType}: A many-to-one relationship defining the item type.
 * - {@code TimePeriod}: A many-to-one relationship defining the time period associated with this item.
 * This class supports both no-argument and full-argument constructors, allowing the
 * creation of fully populated item instances or empty instances for further manipulation.
 */
@lombok.Data
@Entity
@Table(name = "items", schema = "fpfl")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID UserId;

    @NotNull
    @Column(name = "name", nullable = false)
    private String Name;

    @Column(name = "amount")
    private Double Amount;

    /**
     * Represents the type of item associated with this entity.
     * This is a Many-to-One relationship, indicating that multiple entities
     * can be associated with a single item type.
     * The association is lazily loaded, meaning that the item type details
     * will not be fetched from the database until explicitly accessed.
     * The foreign key column in the database is named "fk_item_type", and
     * it is a mandatory field, with null values not allowed.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_item_type", nullable = false)
    private ItemType ItemType;

    /**
     * Represents the associated time period for a specific entity.
     * This variable is mapped as a many-to-one relationship to the TimePeriod entity.
     * The relationship is lazy-loaded and mandatory, ensuring this field cannot be null.
     * - `@ManyToOne` indicates a many-to-one relationship between the owning entity and the TimePeriod entity.
     * - `fetch = FetchType.LAZY` specifies that the associated TimePeriod entity will be loaded lazily.
     * - `@JoinColumn(name = "fk_time_period", nullable = false)` defines the foreign key column name in the database
     * and mandates that the column cannot contain null values.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_time_period", nullable = false)
    private TimePeriod TimePeriod;

    @Column(name = "begin_date")
    private LocalDate BeginDate;

    @Column(name = "end_date")
    private LocalDate EndDate;

    @Column(name = "weekly_dow")
    private Integer WeeklyDow;

    @Column(name = "ever_other_week_dow")
    private Integer EverOtherWeekDow;

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

    @NotNull
    @Column(name = "date_range_req", nullable = false, columnDefinition = "BIT DEFAULT B'0'")
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
    public Item(Long id, UUID userId, String name, Double amount, ItemType itemType,
                TimePeriod timePeriod, LocalDate beginDate, LocalDate endDate, Integer weeklyDow,
                Integer everOtherWeekDow, Integer biMonthlyDay1, Integer biMonthlyDay2, Integer monthlyDom,
                Integer quarterly1Month, Integer quarterly1Day, Integer quarterly2Month, Integer quarterly2Day,
                Integer quarterly3Month, Integer quarterly3Day, Integer quarterly4Month, Integer quarterly4Day,
                Integer semiAnnual1Month, Integer semiAnnual1Day, Integer semiAnnual2Month,
                Integer semiAnnual2Day, Integer annualMoy, Integer annualDom, Boolean dateRangeReq) {
        this.id          = id;
        UserId           = userId;
        Name             = name;
        Amount           = amount;
        ItemType         = itemType;
        TimePeriod       = timePeriod;
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
