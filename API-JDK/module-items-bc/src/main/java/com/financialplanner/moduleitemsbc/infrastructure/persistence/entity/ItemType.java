package com.financialplanner.moduleitemsbc.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * Represents an entity for item types in the financial planner system.
 * This class is mapped to the "item_types" table within the "fpfl" schema.
 * It holds the identifier and name of the item type.
 */
@lombok.Data
@Entity
@Table(name = "item_types", schema = "fpfl")
public class ItemType {

    @Id
    @NotNull
    @Column(name = "id", nullable = false)
    private Long Id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String Name;

    public ItemType() {}

    public ItemType(Long id, String name) {
        this.Id   = id;
        this.Name = name;
    }
}
