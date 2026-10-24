package com.financialplanner.moduleitemsbc.infrastructure.persistence.repository.entity;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Debt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/** Spring Data access to {@code fpfl.debts}. */
@Repository
public interface JpaDebtRepository extends JpaRepository<Debt, Long> {
    List<Debt> findByUserIdOrderByIdAsc(UUID userId);
}
