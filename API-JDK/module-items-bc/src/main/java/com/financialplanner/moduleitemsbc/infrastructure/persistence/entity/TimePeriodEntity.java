package com.financialplanner.moduleitemsbc.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@lombok.Data
@Entity
@Table(name = "time_periods", schema = "fpfl")
public class TimePeriodEntity {
    @Id
    @NotNull
    @Column(name = "id", nullable = false)
    private Long Id;
    @NotNull
    @Column(name = "name", nullable = false)
    private String Name;
}
