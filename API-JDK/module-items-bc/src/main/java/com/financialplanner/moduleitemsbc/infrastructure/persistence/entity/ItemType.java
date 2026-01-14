package com.financialplanner.moduleitemsbc.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * Represents the entity for item types in the persistence layer.
 * This class maps to the "item_types" table in the "fpfl" schema.
 * It is primarily used to define types or categories of items.
 * An item type is identified by its unique ID and associated name.
 * Key Characteristics:
 * - The "id" field serves as the primary key for this entity.
 * - The "name" field represents the name of the item type and is required.
 * This entity may be referenced by other entities, such as {@code Item},
 * to associate specific items with their respective types.
 */
@lombok.Data
@Entity
@Table(name = "item_types", schema = "fpfl")
public class ItemType {
    /**
     * Represents the unique identifier for the {@code ItemType}.
     * This field is the primary key in the "item_types" table within the "fpfl" schema.
     * Characteristics:
     * - It is annotated with {@code @Id}, indicating it is the primary key.
     * - It is a non-nullable field, enforced by {@code @NotNull} and the {@code nullable = false} attribute.
     * - Mapped to the "id" column in the database table through the {@code @Column} annotation.
     */
    @Id
    @NotNull
    @Column(name = "id", nullable = false)
    private Long Id;
    /**
     * Represents the name of the item type.
     * This field is mandatory and is bound to the "name" column in the "item_types" table.
     * It holds the descriptive name of the item type and is used to categorize specific items.
     * The value of this field must not be null and adheres to validation constraints.
     */
    @NotNull
    @Column(name = "name", nullable = false)
    private String Name;

    /**
     * Default constructor for the ItemType class.
     * Instantiates a new instance of the ItemType entity without setting any properties.
     * This constructor is typically used by frameworks or libraries (such as JPA)
     * that require a no-argument constructor to create instances of the entity.
     */
    public ItemType() {}

    /**
     * Constructs an instance of {@code ItemType} with the specified ID and name.
     *
     * @param id   the unique identifier for the item type, must not be null
     * @param name the name of the item type, must not be null
     */
    public ItemType(Long id, String name) {
        this.Id   = id;
        this.Name = name;
    }
}
