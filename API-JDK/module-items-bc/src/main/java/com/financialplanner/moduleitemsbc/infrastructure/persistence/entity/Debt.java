package com.financialplanner.moduleitemsbc.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Debt Paydown: a debit the user is paying down (for example a credit card's monthly payment), with what is
 * owed on it. Maps to {@code fpfl.debts}, the same table API-NET uses (see {@code db/001_debt_paydown.sql}).
 * <p>
 * Deleting the debit deletes its tracked debt ({@code ON DELETE CASCADE}). Getters/setters only (no
 * {@code @Data}): the debt and its allocations point at each other, so a generated equals/hashCode/toString
 * would recurse.
 */
@Getter
@Setter
@Entity
@Table(name = "debts", schema = "fpfl", indexes = @Index(name = "ix_debts_user_id", columnList = "user_id"))
public class Debt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Owner: {@code users.user_id}, always the same as the debit item's owner. */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /** The debit whose scheduled payments pay this debt down. One debt per debit. */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "fk_item", nullable = false, unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Item item;

    /** Amount owed at the end of {@link #balanceDate}. */
    @Column(name = "balance", nullable = false, precision = 14, scale = 2)
    private BigDecimal balance;

    /** Annual percentage rate, e.g. 24.99; 0 for none. Charged monthly as APR / 12. */
    @Column(name = "apr", nullable = false, precision = 6, scale = 3)
    private BigDecimal apr = BigDecimal.ZERO;

    /** The day {@link #balance} was true; payments after it reduce the balance. */
    @Column(name = "balance_date", nullable = false)
    private LocalDate balanceDate;

    /** Set by the database. */
    @Column(name = "created_at", insertable = false, updatable = false,
            columnDefinition = "timestamptz NOT NULL DEFAULT now()")
    private Instant createdAt;

    @OneToMany(mappedBy = "debt", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<DebtAllocation> allocations = new ArrayList<>();

    public Debt() {}
}
