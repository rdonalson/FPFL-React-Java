package com.financialplanner.moduleitemsbc.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents an item entity with various attributes and details related to
 * scheduling, monetary value, and associations with other entities.
 * ---
 * The ItemEntity class is used to encapsulate data for an item, including
 * scheduling attributes for different frequencies (e.g., weekly, bi-monthly,
 * quarterly, annual), monetary amounts, date ranges, and associations with
 * related entities such as ItemTypeEntity and TimePeriodEntity. It also provides
 * mechanisms to store identifiers for both the item itself and the associated user.
 */
@lombok.Data
@Entity @Table(name = "items", schema = "fpfl")
public class ItemEntity {
    /**
     * Represents the unique identifier for the {@code ItemEntity}.
     * This field is automatically generated using the {@code GenerationType.IDENTITY} strategy.
     * It is a non-nullable column in the database represented by the name "id".
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull @Column(name = "id", nullable = false)
    private Long id;
    /**
     * Represents the unique identifier for the user associated with the item entity.
     * This field is mandatory and corresponds to the "user_id" column in the database.
     */
    @NotNull @Column(name = "user_id", nullable = false)
    private UUID UserId;
    /**
     * Represents the name of the item associated with this entity.
     * This field is mandatory and cannot be null.
     * It maps to the "name" column in the "items" table within the "fpfl" schema.
     */
    @NotNull  @Column(name = "name", nullable = false)
    private String Name;
    /**
     * Represents the monetary amount associated with the item.
     * This value specifies the financial value or cost related to the entity.
     * It is mapped to the "amount" column in the database.
     */
    @Column(name = "amount")
    private Double Amount;

    /**
     * Represents the relationship between the ItemEntity and the ItemTypeEntity.
     * This field establishes a many-to-one association with the ItemTypeEntity,
     * which represents the type or category of the item.
     * ---
     * Constraints:
     * - This association is mandatory (cannot be null).
     * - Fetch type is configured as lazy, meaning the related ItemTypeEntity will be
     *   loaded only when explicitly accessed.
     * - The foreign key column name in the database is 'fk_item_type', and it does not allow null values.
     * ---
     * Mapping Details:
     * - Many-to-One relationship: An item can belong to one item type, but an item type can have multiple items.
     * - Foreign Key: The 'fk_item_type' column maps this relationship to the appropriate
     *   ItemTypeEntity record in the database schema.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_item_type", nullable = false)
    private ItemTypeEntity ItemType;

    /**
     * Represents the association to a specific time period for the {@link ItemEntity}.
     * The reference is made to the {@link TimePeriodEntity} table.
     * ---
     * This field establishes a many-to-one relationship, indicating that multiple
     * {@link ItemEntity} instances can reference the same {@link TimePeriodEntity}.
     * ---
     * Constraints:
     * - The relationship is mandatory (non-null).
     * - Fetch type is set to LAZY to optimize data loading.
     * ---
     * Database Mapping:
     * - Mapped to the "fk_time_period" column in the "items" table.
     * - This column cannot hold null values.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_time_period", nullable = false)
    private TimePeriodEntity TimePeriod;

    /**
     * Represents the starting date for an entity or schedule.
     * This value typically marks the commencement of a time-bound operation
     * or validity period associated with the item.
     */
    @Column(name = "begin_date")
    private LocalDate BeginDate;
    /**
     * Represents the end date associated with an item in the system.
     * This date typically marks the conclusion of an item's active period
     * or the end of a valid date range for the item.
     */
    @Column(name = "end_date")
    private LocalDate EndDate;
    /**
     * Represents the day of the week for a recurring weekly schedule.
     * ---
     * The value is stored as an integer where 1 corresponds to Monday, 2 corresponds to Tuesday,
     * and so on until 7, which corresponds to Sunday. This field is used to identify the specific day in a week
     * when an activity or event recurs.
     * ---
     * Mapped to the "weekly_dow" column in the database.
     */
    @Column(name = "weekly_dow")
    private Integer WeeklyDow;
    /**
     * Represents the day of the week for an event or occurrence that happens every other week.
     * The value is stored as an integer, where the mapping to days of the week
     * is dependent on the application's interpretation (e.g., 1 for Monday, 7 for Sunday).
     * This field allows tracking and scheduling of bi-weekly events.
     */
    @Column(name = "ever_other_week_dow")
    private Integer EverOtherWeekDow;
    /**
     * Represents the first day in a bi-monthly schedule configuration.
     * This value determines one of the two specific days within a month
     * when an action or event is scheduled to occur as part of a bi-monthly cadence.
     *
     * Constraints/Usage:
     * 1. Must be a valid day of the month, ranging from 1 to 31, depending on the month.
     * 2. Works in conjunction with {@link BiMonthlyDay2} to define the two days within the bi-monthly cycle.
     *
     * Database Mapping:
     * Mapped to the "bi_monthly_day_1" column in the database.
     */
    @Column(name = "bi_monthly_day_1")
    private Integer BiMonthlyDay1;
    /**
     * Represents the second day of a bi-monthly recurring time period.
     * This field is used to specify the second selected day in cases where
     * an event or item occurs twice within a calendar month.
     *
     * The value of this field is stored in the database column `bi_monthly_day_2`.
     * It is expected to hold an integer value corresponding to a day of the month,
     * with valid values ranging from 1 to 31, depending on the use case.
     */
    @Column(name = "bi_monthly_day_2")
    private Integer BiMonthlyDay2;
    /**
     * Represents the specific day of the month associated with this item entity.
     * This field stores an integer value which denotes the day of the month (1-31).
     * It is mapped to the column "monthly_dom" in the database table.
     */
    @Column(name = "monthly_dom")
    private Integer MonthlyDom;
    /**
     * Represents the first month of a quarterly recurrence for an item entity.
     * This value specifies which month within a quarter the recurrence starts.
     * The value can range from 1 (January) to 12 (December).
     */
    @Column(name = "quarterly_1_month")
    private Integer Quarterly1Month;
    /**
     * Represents the day of the month associated with the first quarterly occurrence
     * for a specific item. This value is used in conjunction with {@code Quarterly1Month}
     * to define a scheduled date within the first quarter.
     *
     * Mapped to the database column "quarterly_1_day".
     */
    @Column(name = "quarterly_1_day")
    private Integer Quarterly1Day;
    /**
     * Represents the second month's offset in a quarterly schedule.
     * This field holds the month number (1-12) that corresponds to
     * the second month of a quarter for a specific item or financial plan.
     * It is mapped to the database column "quarterly_2_month".
     */
    @Column(name = "quarterly_2_month")
    private Integer Quarterly2Month;
    /**
     * Represents the second day of a designated time interval for a quarterly period,
     * corresponding to the second month defined in the schedule.
     * This value can be used to specify a particular day within the second quarter month
     * for scheduling or financial planning purposes.
     *
     * This field is mapped to the "quarterly_2_day" column in the database.
     */
    @Column(name = "quarterly_2_day")
    private Integer Quarterly2Day;
    /**
     * Represents the month of the third quarter in a quarterly frequency configuration.
     * This value is expected to define or reference the specific month
     * (e.g., 1 for January, 2 for February, etc.) within the time period
     * associated with quarterly financial or scheduling data.
     */
    @Column(name = "quarterly_3_month")
    private Integer Quarterly3Month;
    /**
     * Represents the day of the third occurrence within a quarterly time period.
     * This value is used for scheduling or defining specific dates in a recurring
     * quarterly pattern.
     *
     * The field maps to the database column "quarterly_3_day".
     */
    @Column(name = "quarterly_3_day")
    private Integer Quarterly3Day;
    /**
     * Represents the month associated with the fourth quarter
     * in a quarterly recurrence configuration for an item entity.
     * This value specifies the month number (1-12) during which
     * the fourth quarter starts or is relevant.
     */
    @Column(name = "quarterly_4_month")
    private Integer Quarterly4Month;
    /**
     * Represents the day component of the fourth quarterly occurrence for a financial item.
     * This field stores an integer value indicating the specific day of the month
     * associated with the fourth quarter's recurrence schedule.
     *
     * Column: `quarterly_4_day`
     * Mapped to the corresponding database column in the "items" table of the "fpfl" schema.
     */
    @Column(name = "quarterly_4_day")
    private Integer Quarterly4Day;
    /**
     * Represents the first month in a semi-annual time period for the corresponding item.
     * This value is stored in the database column "semi_annual_1_month".
     * It is used to denote which specific month marks the starting point of the
     * first semi-annual period associated with the item.
     */
    @Column(name = "semi_annual_1_month")
    private Integer SemiAnnual1Month;
    /**
     * Represents the day of the first payment or occurrence in a semi-annual time period.
     * This value specifies the day within the month defined in {@code SemiAnnual1Month}.
     * Typically used in financial planning or scheduling scenarios to determine
     * semi-annual payment or activity dates.
     */
    @Column(name = "semi_annual_1_day")
    private Integer SemiAnnual1Day;
    /**
     * Represents the second month in a semi-annual schedule.
     * This variable is used to store the month of the year (1 - 12)
     * corresponding to the second occurrence within a semi-annual frequency.
     * Maps to the database column "semi_annual_2_month".
     */
    @Column(name = "semi_annual_2_month")
    private Integer SemiAnnual2Month;
    /**
     * Represents the day component of the second semi-annual time period configuration
     * for an item. This field is used to specify on which day of the month the second
     * semi-annual recurrence should occur.
     *
     * The value is stored as an Integer in the database column "semi_annual_2_day".
     */
    @Column(name = "semi_annual_2_day")
    private Integer SemiAnnual2Day;
    /**
     * Represents the numeric value corresponding to a specific month of the year
     * for annual occurrences. This field is used to denote the month when
     * an annual event or transaction takes place.
     *
     * Constraints:
     * - Stored in the column "annual_moy" in the database.
     * - Values typically range from 1 (January) to 12 (December).
     */
    @Column(name = "annual_moy")
    private Integer AnnualMoy;
    /**
     * Represents the specific day of the month associated with an annual time period.
     * This value indicates which day of the month is relevant for an operation
     * or entity that occurs annually.
     * It can be null if the context does not require a specific day for annual occurrences.
     */
    @Column(name = "annual_dom")
    private Integer AnnualDom;
    /**
     * Indicates whether a date range is required for the associated item.
     *
     * This field is stored as a non-nullable column in the database with a default value of {@code false}.
     * It is used to denote if specific date range values (e.g., BeginDate and EndDate) are mandatory for the item.
     */
    @NotNull @Column(name = "date_range_req", nullable = false, columnDefinition = "BIT DEFAULT B'0'")
    private Boolean DateRangeReq = false;

    /**
     * Default constructor for the ItemEntity class.
     * Initializes an instance of ItemEntity with default values.
     */
    public ItemEntity() { }
    /**
     * Constructs an instance of ItemEntity with the provided parameters.
     *
     * @param id The unique identifier of the item.
     * @param userId The unique identifier of the user associated with this item.
     * @param name The name of the item.
     * @param amount The monetary amount associated with the item.
     * @param itemType The type of the item (reference to ItemTypeEntity).
     * @param timePeriod The time period associated with the item (reference to TimePeriodEntity).
     * @param beginDate The beginning date for the item.
     * @param endDate The ending date for the item.
     * @param weeklyDow The day of the week for weekly occurrences.
     * @param everOtherWeekDow The day of the week for every other week occurrences.
     * @param biMonthlyDay1 The first day of the period for bi-monthly occurrences.
     * @param biMonthlyDay2 The second day of the period for bi-monthly occurrences.
     * @param monthlyDom The day of the month for monthly occurrences.
     * @param quarterly1Month The first month for quarterly occurrences.
     * @param quarterly1Day The first day for quarterly occurrences.
     * @param quarterly2Month The second month for quarterly occurrences.
     * @param quarterly2Day The second day for quarterly occurrences.
     * @param quarterly3Month The third month for quarterly occurrences.
     * @param quarterly3Day The third day for quarterly occurrences.
     * @param quarterly4Month The fourth month for quarterly occurrences.
     * @param quarterly4Day The fourth day for quarterly occurrences.
     * @param semiAnnual1Month The first month for semi-annual occurrences.
     * @param semiAnnual1Day The first day for semi-annual occurrences.
     * @param semiAnnual2Month The second month for semi-annual occurrences.
     * @param semiAnnual2Day The second day for semi-annual occurrences.
     * @param annualMoy The month of the year for annual occurrences.
     * @param annualDom The day of the month for annual occurrences.
     * @param dateRangeReq Indicates whether a date range is required for the item (true/false).
     */
    public ItemEntity(Long id, UUID userId, String name, Double amount, ItemTypeEntity itemType, TimePeriodEntity timePeriod, LocalDate beginDate, LocalDate endDate, Integer weeklyDow, Integer everOtherWeekDow, Integer biMonthlyDay1, Integer biMonthlyDay2, Integer monthlyDom, Integer quarterly1Month, Integer quarterly1Day, Integer quarterly2Month, Integer quarterly2Day, Integer quarterly3Month, Integer quarterly3Day, Integer quarterly4Month, Integer quarterly4Day, Integer semiAnnual1Month, Integer semiAnnual1Day, Integer semiAnnual2Month, Integer semiAnnual2Day, Integer annualMoy, Integer annualDom, Boolean dateRangeReq) {
        this.id = id;
        UserId = userId;
        Name = name;
        Amount = amount;
        ItemType = itemType;
        TimePeriod = timePeriod;
        BeginDate = beginDate;
        EndDate = endDate;
        WeeklyDow = weeklyDow;
        EverOtherWeekDow = everOtherWeekDow;
        BiMonthlyDay1 = biMonthlyDay1;
        BiMonthlyDay2 = biMonthlyDay2;
        MonthlyDom = monthlyDom;
        Quarterly1Month = quarterly1Month;
        Quarterly1Day = quarterly1Day;
        Quarterly2Month = quarterly2Month;
        Quarterly2Day = quarterly2Day;
        Quarterly3Month = quarterly3Month;
        Quarterly3Day = quarterly3Day;
        Quarterly4Month = quarterly4Month;
        Quarterly4Day = quarterly4Day;
        SemiAnnual1Month = semiAnnual1Month;
        SemiAnnual1Day = semiAnnual1Day;
        SemiAnnual2Month = semiAnnual2Month;
        SemiAnnual2Day = semiAnnual2Day;
        AnnualMoy = annualMoy;
        AnnualDom = annualDom;
        DateRangeReq = dateRangeReq;
    }
}
