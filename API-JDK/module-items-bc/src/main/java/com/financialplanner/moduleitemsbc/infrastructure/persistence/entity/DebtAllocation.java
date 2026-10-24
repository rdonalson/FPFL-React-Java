package com.financialplanner.moduleitemsbc.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;

/**
 * Debt Paydown: a share of a credit sent to a debt every time the credit occurs, e.g. 30% of each paycheck.
 * Maps to {@code fpfl.debt_allocations}. Deleting the credit or the debt deletes the allocation.
 */
@Getter
@Setter
@Entity
@Table(name = "debt_allocations", schema = "fpfl",
       uniqueConstraints = @UniqueConstraint(columnNames = {"fk_debt", "fk_credit_item"}),
       indexes = @Index(name = "ix_debt_allocations_credit", columnList = "fk_credit_item"))
public class DebtAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_debt", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Debt debt;

    /** The credit the money comes from. */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "fk_credit_item", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Item creditItem;

    /** Share of each occurrence of the credit, above 0 and at most 100. */
    @Column(name = "percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal percent;

    public DebtAllocation() {}
}
