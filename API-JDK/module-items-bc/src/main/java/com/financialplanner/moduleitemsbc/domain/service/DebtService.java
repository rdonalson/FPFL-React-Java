package com.financialplanner.moduleitemsbc.domain.service;

import com.financialplanner.moduleitemsbc.domain.debt.DebtDraft;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Debt;

import java.util.List;
import java.util.UUID;

/**
 * Debt Paydown: a user's tracked debts and the credits allocated to them. A user only sees and changes
 * their own (someone else's answers 404 unless the requester is an admin).
 */
public interface DebtService {

    List<Debt> list(UUID userId);

    /** @throws com.financialplanner.modulecommonbc.exception.ItemNotFoundException missing, or someone else's */
    Debt get(Long id, UUID requesterUserId, boolean requesterIsAdmin);

    /** Starts tracking one of the owner's debits. */
    Debt create(DebtDraft draft, UUID ownerUserId);

    /** Changes the balance, APR, balance date and allocations; the debit itself can't change. */
    Debt update(Long id, DebtDraft draft, UUID requesterUserId, boolean requesterIsAdmin);

    /** Stops tracking the debt. The debit item itself is untouched. */
    void delete(Long id, UUID requesterUserId, boolean requesterIsAdmin);
}
