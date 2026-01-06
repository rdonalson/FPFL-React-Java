package com.financialplanner.moduleapi.dto;

import lombok.Getter;

/**
 * Represents a Data Transfer Object (DTO) for an item type.
 * This class is designed to carry data related to item types between different layers
 * of an application, such as services and controllers.
 * The class enforces validation on its attributes to ensure data integrity.
 */
public class ItemTypeDto {

    /**
     * Represents the unique identifier for an item type.
     * This field holds a numeric value used to uniquely identify an instance of the item type.
     */
    @Getter
    private Long id;
    /**
     * Represents the name of the item type.
     * This field holds the descriptive name associated with a specific item type.
     */
    @Getter
    private String name;

    /**
     * Default constructor for the ItemTypeDto class.
     * Initializes a new instance of the ItemTypeDto object with default values.
     */
    public ItemTypeDto() {}

    /**
     * Sets the unique identifier of the item type.
     * The identifier cannot be null; otherwise, an IllegalArgumentException is thrown.
     *
     * @param id the unique identifier to set for the item type
     * @throws IllegalArgumentException if the provided id is null
     */
    public void setId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id is required");
        }
        this.id = id;
    }

    /**
     * Sets the name of the item type.
     * The name cannot be null or blank; otherwise, an IllegalArgumentException is thrown.
     *
     * @param name the name to set for the item type
     * @throws IllegalArgumentException if the provided name is null or blank
     */
    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        this.name = name;
    }
}
