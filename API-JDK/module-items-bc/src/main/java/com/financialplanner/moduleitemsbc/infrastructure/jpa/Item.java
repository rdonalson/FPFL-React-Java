package com.financialplanner.moduleitemsbc.infrastructure.jpa;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

@lombok.Data
@Entity
@Table(name = "Items", schema = "Item-Detail")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private UUID UserId;
    private String Name;
    private @NotNull Double Amount;
    private @NotNull Integer FkItemType;
    private @NotNull Integer FkPeriod;
    private @NotNull LocalDate BeginDate;
    private @NotNull LocalDate EndDate;
    private @NotNull Integer WeeklyDow;
    private @NotNull Integer EverOtherWeekDow;
    private @NotNull Integer BiMonthlyDay1;
    private @NotNull Integer BiMonthlyDay2;
    private @NotNull Integer MonthlyDom;
    private @NotNull Integer Quarterly1Month;
    private @NotNull Integer Quarterly1Day;
    private @NotNull Integer Quarterly2Month;
    private @NotNull Integer Quarterly2Day;
    private @NotNull Integer Quarterly3Month;
    private @NotNull Integer Quarterly3Day;
    private @NotNull Integer Quarterly4Month;
    private @NotNull Integer Quarterly4Day;
    private @NotNull Integer SemiAnnual1Month;
    private @NotNull Integer SemiAnnual1Day;
    private @NotNull Integer SemiAnnual2Month;
    private @NotNull Integer SemiAnnual2Day;
    private @NotNull Integer AnnualMoy;
    private @NotNull Integer AnnualDom;
    private Boolean DateRangeReq;
}
