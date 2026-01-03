package com.financialplanner.moduleapi.dto;

/**
 * Data Transfer Object (DTO) for representing an item type.
 * This class is used to encapsulate the data related to an item type, including its unique identifier and name.
 * It serves as a lightweight object for data exchange between application layers.
 */
@lombok.Data
public class ItemTypeDto {
    /**
     * Represents the unique identifier for an instance of the entity.
     * This field is used to differentiate between different instances of the same entity class.
     */
    private Long id;
    /**
     * Represents the name of the item type.
     * This field is typically used to describe or identify an item type in human-readable form.
     */
    private String name;

    /**
     * Default constructor for the ItemTypeDto class.
     * Initializes a new instance of the ItemTypeDto class without setting any values for its fields.
     */
    public ItemTypeDto() {
    }

    /**
     * Constructs an instance of the ItemTypeDto class with the specified parameters.
     * -----------
     *
     * @param id   the unique identifier of the item type
     * @param name the name of the item type
     */
    public ItemTypeDto(Long id,
                       String name
                      ) {
        this.id   = id;
        this.name = name;
    }
}
