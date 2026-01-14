package com.financialplanner.moduleitemsbc.domain.model;

import jakarta.validation.constraints.NotNull;

/**
 * Represents a time period with a unique identifier and a descriptive name.
 * This class is primarily intended for distinguishing and managing various
 * periods of time within the application.
 */
@lombok.Data
public class TimePeriod {
    /**
     * Represents the unique identifier for a time period.
     * This field is mandatory and must not be null.
     * It serves as the primary key for identifying individual time periods
     * within the system.
     */
    @NotNull
    private Long Id;
    /**
     * Represents the name of the time period.
     * This field is mandatory and cannot be null.
     * It is used to store a meaningful, descriptive identifier
     * for the specific time period in the system.
     */
    @NotNull
    private String Name;

    /**
     * Default constructor for the TimePeriod class.
     * Initializes a new instance of the TimePeriod class with default or uninitialized property values.
     */
    public TimePeriod() {
    }

    /**
     * Constructs a new TimePeriod instance with the specified ID and name.
     *
     * @param id   the unique identifier for the time period; must not be null
     * @param name the name of the time period; must not be null
     */
    public TimePeriod(Long id, String name) {
        this.Id   = id;
        this.Name = name;
    }
}
