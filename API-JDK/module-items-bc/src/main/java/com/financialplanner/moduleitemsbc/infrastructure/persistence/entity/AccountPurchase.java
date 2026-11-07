package com.financialplanner.moduleitemsbc.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Cards & Loans: a purchase charged to a {@link CreditAccount}. Maps to {@code fpfl.account_purchases}.
 * Scheduled like a debit item, limited for now to One Time (1), Daily (2) and Weekly (3); see
 * {@code PurchaseRules}. Deleting the account deletes its purchases.
 */
@Getter
@Setter
@Entity
@Table(name = "account_purchases", schema = "fpfl",
       indexes = @Index(name = "ix_account_purchases_account", columnList = "fk_account"))
public class AccountPurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_account", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private CreditAccount account;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "fk_time_period", nullable = false)
    private TimePeriod timePeriod;

    @Column(name = "begin_date")
    private LocalDate beginDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    /** Weekly only: 0 = Sunday … 6 = Saturday. */
    @Column(name = "weekly_dow")
    private Integer weeklyDow;

    @Column(name = "date_range_req", nullable = false)
    private Boolean dateRangeReq = false;

    /** Set by the database. */
    @Column(name = "created_at", insertable = false, updatable = false,
            columnDefinition = "timestamptz NOT NULL DEFAULT now()")
    private Instant createdAt;

    public AccountPurchase() {}

    /** The owning account's id, without loading the account. */
    public Long getAccountId() {
        return account == null ? null : account.getId();
    }
}
