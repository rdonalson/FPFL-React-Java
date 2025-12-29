package com.financialplanner.moduleitemsbc.domain.model;

import jakarta.validation.constraints.NotNull;

/**
 * Represents a type or category of an item.
 * The ItemType class is used to define attributes that uniquely identify and describe
 * a particular type of item within the system. It acts as a classification mechanism
 * for items, enabling better organization and management of item data.
 */
@lombok.Data
public class ItemType {
    /**
     * Represents the unique identifier for the item type.
     * This field is mandatory and must not be null.
     */
    @NotNull
    private Long Id;
    /**
     * Represents the name of the item type. This field is a mandatory attribute and
     * cannot be null. It is used to provide a meaningful, descriptive identifier for
     * the type of item in the system.
     */
    @NotNull
    private String Name;

    /**
     * Default constructor for the ItemType class.
     * Initializes an instance of ItemType with default or uninitialized property values.
     */
    public ItemType() {}
    /**
     * Constructs a new instance of the ItemType class with the specified ID and name.
     *
     * @param id   the unique identifier for the item type; must not be null
     * @param name the name of the item type; must not be null
     */
    public ItemType(Long id, String name) {
        this.Id = id;
        this.Name = name;
    }

}
