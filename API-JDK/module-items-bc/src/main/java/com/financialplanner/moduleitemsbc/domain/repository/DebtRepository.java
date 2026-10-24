package com.financialplanner.moduleitemsbc.domain.repository;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Debt;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Debt Paydown's tracked debts (with their allocations). */
public interface DebtRepository {
    List<Debt> findByUserId(UUID userId);
    Optional<Debt> findById(Long id);
    Debt save(Debt debt);
    void deleteById(Long id);
}
