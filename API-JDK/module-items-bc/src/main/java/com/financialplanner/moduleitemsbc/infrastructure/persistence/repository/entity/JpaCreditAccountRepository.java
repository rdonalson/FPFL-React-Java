package com.financialplanner.moduleitemsbc.infrastructure.persistence.repository.entity;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.CreditAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/** Spring Data access to {@code fpfl.credit_accounts}. */
@Repository
public interface JpaCreditAccountRepository extends JpaRepository<CreditAccount, Long> {
    List<CreditAccount> findByUserIdOrderByNameAscIdAsc(UUID userId);
}
