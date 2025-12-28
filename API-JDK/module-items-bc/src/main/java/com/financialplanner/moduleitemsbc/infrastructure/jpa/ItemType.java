package com.financialplanner.moduleitemsbc.infrastructure.jpa;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@lombok.Data
@Entity
@Table(name = "item_types", schema = "fpfl")
public class ItemType {
    @Id
    @NotNull @Column(name = "id", nullable = false)
    private Long Id;
    @NotNull  @Column(name = "name", nullable = false)
    private String Name;
}
