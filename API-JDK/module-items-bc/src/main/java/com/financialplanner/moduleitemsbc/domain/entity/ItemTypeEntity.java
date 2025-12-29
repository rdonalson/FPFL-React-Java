package com.financialplanner.moduleitemsbc.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@lombok.Data
@Entity @Table(name = "item_types", schema = "fpfl")
public class ItemTypeEntity {
    @Id @NotNull @Column(name = "id", nullable = false)
    private Long Id;
    @NotNull  @Column(name = "name", nullable = false)
    private String Name;
}
