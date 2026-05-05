package com.financialplanner.moduleauth.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@lombok.Data
@Entity
@Table(name = "refresh_tokens", schema = "fpfl")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private Instant expiryDate;

    public RefreshToken() {}
}
