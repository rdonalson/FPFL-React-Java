package com.financialplanner.moduleauth.infrastructure.persistence.entity;

import jakarta.persistence.*;

@lombok.Data
@Entity
@Table(name = "roles", schema = "fpfl")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // e.g. "ROLE_ADMIN"

    public Role() {}

    public Role(String name) {
        this.name = name;
    }
}
