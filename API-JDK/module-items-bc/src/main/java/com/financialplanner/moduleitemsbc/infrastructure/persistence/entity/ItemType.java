package com.financialplanner.moduleitemsbc.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * Represents an item type entity that is persisted in the database.
 * This class is annotated with JPA and validation annotations to define
 * the database schema mapping and field constraints.
 *
 * The {@code ItemType} entity is part of the {@code fpfl} schema and maps to
 * the "item_types" table. It contains fields for a unique identifier and
 * a name to describe the item type.
 *
 * Annotations:
 * - {@code @Entity}: Marks this class as a JPA entity.
 * - {@code @Table(name = "item_types", schema = "fpfl")}: Specifies the table
 *   name and schema under which this entity is maintained.
 * - {@code @lombok.Data}: Automatically generates boilerplate code such as
 *   getters, setters, equals, hashCode, and toString methods.
 */
@lombok.Data
@Entity
@Table(name = "item_types", schema = "fpfl")
public class ItemType {
    /**
     * Represents the unique identifier for the item type entity.
     * Acts as the primary key and ensures each item type is uniquely
     * identifiable within the persistence layer.
     *
     * Constraints:
     * - Must not be null.
     * - Mapped to the "id" column in the "item_types" table.
     */
    @Id
    @NotNull
    @Column(name = "id", nullable = false)
    private Long Id;
    /**
     * Represents the name of the item type.
     * This field is mandatory and cannot be null.
     * It is mapped to the "name" column in the "item_types" table within the "fpfl" schema.
     * Used to define a descriptive label or title for the item type.
     */
    @NotNull
    @Column(name = "name", nullable = false)
    private String Name;

    /**
     * Default no-argument constructor for the {@code ItemType} class.
     * This constructor initializes a new instance of the {@code ItemType} entity
     * without setting any of its properties. It is primarily used by frameworks
     * such as JPA for entity instantiation.
     */
    public ItemType() {}

    /**
     * Constructs a new ItemType instance with the specified ID and name.
     *
     * @param id   the unique identifier for the item type. Must not be null.
     * @param name the name of the item type. Must not be null.
     */
    public ItemType(Long id, String name) {
        this.Id   = id;
        this.Name = name;
    }
}
