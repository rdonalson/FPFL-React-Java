package com.financialplanner.moduleitemsbc.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Cards & Loans: a credit card or loan with its own running balance. Purchases raise the balance; payments
 * from the user's credits and debits lower it. Maps to {@code fpfl.credit_accounts}, the same table API-NET
 * uses (created by API-NET's {@code db/002_credit_accounts.sql}).
 * <p>
 * Both collections are fetched eagerly with a separate select each ({@link FetchMode#SUBSELECT}): two eager
 * lists fetched by join would be a "multiple bags" error. Getters/setters only (no {@code @Data}): the account
 * and its purchases and payments point at each other.
 */
@Getter
@Setter
@Entity
@Table(name = "credit_accounts", schema = "fpfl",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "name"}),
       indexes = @Index(name = "ix_credit_accounts_user_id", columnList = "user_id"))
public class CreditAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Owner: {@code users.user_id}. */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false, length = 20)
    private AccountKind kind = AccountKind.CreditCard;

    /** What was owed at the end of {@link #balanceDate}; activity counts from the next day. */
    @Column(name = "opening_balance", nullable = false, precision = 14, scale = 2)
    private BigDecimal openingBalance;

    @Column(name = "balance_date", nullable = false)
    private LocalDate balanceDate;

    /** Annual percentage rate, 0–100; interest is added at each month end. */
    @Column(name = "apr", nullable = false, precision = 6, scale = 3)
    private BigDecimal apr = BigDecimal.ZERO;

    /** Optional; cards only, for showing available credit. */
    @Column(name = "credit_limit", precision = 14, scale = 2)
    private BigDecimal creditLimit;

    /** Set by the database. */
    @Column(name = "created_at", insertable = false, updatable = false,
            columnDefinition = "timestamptz NOT NULL DEFAULT now()")
    private Instant createdAt;

    @OneToMany(mappedBy = "account", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    @OrderBy("id")
    private List<AccountPurchase> purchases = new ArrayList<>();

    @OneToMany(mappedBy = "account", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    @OrderBy("id")
    private List<AccountPayment> payments = new ArrayList<>();

    public CreditAccount() {}
}
