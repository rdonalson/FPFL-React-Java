package com.financialplanner.moduleitemsbc.domain.repository;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.CreditAccount;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Cards & Loans; each account comes with its purchases and payments. */
public interface CreditAccountRepository {
    List<CreditAccount> findByUserId(UUID userId);
    Optional<CreditAccount> findById(Long id);
    /** Saves the account with its purchases and payments (cascade). */
    CreditAccount save(CreditAccount account);
    void deleteById(Long id);
}
