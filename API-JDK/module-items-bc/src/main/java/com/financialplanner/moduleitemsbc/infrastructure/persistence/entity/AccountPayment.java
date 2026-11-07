package com.financialplanner.moduleitemsbc.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;

/**
 * Cards & Loans: one of the user's items paying a {@link CreditAccount}. Maps to {@code fpfl.account_payments}.
 * A credit sends either {@link #amount} or {@link #percent} of each occurrence (exactly one is set); a debit
 * (e.g. the card's monthly payment) pays its whole amount and is stored as 100%. Deleting the account or the
 * item deletes the payment.
 */
@Getter
@Setter
@Entity
@Table(name = "account_payments", schema = "fpfl",
       uniqueConstraints = @UniqueConstraint(columnNames = {"fk_account", "fk_credit_item"}),
       indexes = @Index(name = "ix_account_payments_credit", columnList = "fk_credit_item"))
public class AccountPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_account", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private CreditAccount account;

    /** The credit or debit the money comes from. */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "fk_credit_item", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Item creditItem;

    @Column(name = "amount", precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "percent", precision = 5, scale = 2)
    private BigDecimal percent;

    public AccountPayment() {}
}
