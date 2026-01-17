package com.financialplanner.moduleitemsbc.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * Represents a TimePeriod entity that is mapped to the "time_periods" table
 * in the "fpfl" schema of the database.
 *
 * The TimePeriod class is used to store information about time periods,
 * including a unique identifier and name. This entity is annotated
 * with JPA and validation constraints to ensure data integrity.
 *
 * Annotations used:
 * - {@code @Entity}: Specifies that this class is an entity.
 * - {@code @Table}: Defines the corresponding table name and schema in the database.
 * - {@code @Id}: Identifies the primary key field.
 * - {@code @Column}: Maps the fields to columns in the database table.
 * - {@code @NotNull}: Ensures that the fields are not null.
 * - {@code @lombok.Data}: Generates getters, setters, equals, hashCode, and toString methods automatically.
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
