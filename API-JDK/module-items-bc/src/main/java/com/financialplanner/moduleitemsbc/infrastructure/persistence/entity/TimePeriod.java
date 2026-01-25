package com.financialplanner.moduleitemsbc.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * Represents a specific time period entity stored in the database.
 * This entity is used for managing and persisting time period data.
 */
@lombok.Data
@Entity
@Table(name = "time_periods", schema = "fpfl")
public class TimePeriod {
    @Id
    @NotNull
    @Column(name = "id", nullable = false)
    private Long Id;
    @NotNull
    @Column(name = "name", nullable = false)
    private String Name;

    public TimePeriod() {}

    public TimePeriod(Long id, String name) {
        this.Id = id;
        this.Name = name;
    }
}
