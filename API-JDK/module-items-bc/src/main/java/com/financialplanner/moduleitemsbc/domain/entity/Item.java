package com.financialplanner.moduleitemsbc.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

@lombok.Data
@Entity
@Table(name = "items", schema = "fpfl")
public class Item {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull @Column(name = "id", nullable = false)
    private Long id;
    @NotNull @Column(name = "user_id", nullable = false)
    private UUID UserId;
    @NotNull  @Column(name = "name", nullable = false)
    private String Name;
    @Column(name = "amount")
    private Double Amount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_item_type", nullable = false)
    private ItemType ItemType;

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
    @NotNull @Column(name = "date_range_req", nullable = false, columnDefinition = "BIT DEFAULT B'0'")
    private Boolean DateRangeReq = false;
}
