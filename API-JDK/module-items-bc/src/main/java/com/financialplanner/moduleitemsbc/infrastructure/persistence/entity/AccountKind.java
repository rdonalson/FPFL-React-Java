package com.financialplanner.moduleitemsbc.infrastructure.persistence.entity;

/**
 * Cards & Loans: what kind of account a {@link CreditAccount} is. Stored by name in
 * {@code fpfl.credit_accounts.kind} ({@code CreditCard} or {@code Loan}), the same values API-NET writes.
 */
public enum AccountKind {
    CreditCard,
    Loan
}
