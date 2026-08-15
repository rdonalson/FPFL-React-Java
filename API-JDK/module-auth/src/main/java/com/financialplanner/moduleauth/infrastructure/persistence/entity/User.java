package com.financialplanner.moduleauth.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@lombok.Data
@Entity
@Table(name = "users", schema = "fpfl")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "created_at", updatable = false, insertable = false)
    private Instant createdAt;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId = UUID.randomUUID();

    @Column(name = "first", length = 50)
    private String first;

    @Column(name = "last", length = 100)
    private String last;

    public User() {}
}
