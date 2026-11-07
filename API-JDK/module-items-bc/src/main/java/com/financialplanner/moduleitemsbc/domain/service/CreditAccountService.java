package com.financialplanner.moduleitemsbc.domain.service;

import com.financialplanner.moduleitemsbc.domain.account.CreditAccountDraft;
import com.financialplanner.moduleitemsbc.domain.account.PurchaseDraft;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.AccountPurchase;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.CreditAccount;

import java.util.List;
import java.util.UUID;

/**
 * Cards & Loans: a user's credit cards and loans, their purchases, and the items that pay them. A user only
 * sees and changes their own (someone else's answers 404 unless the requester is an admin).
 */
public interface CreditAccountService {

    List<CreditAccount> list(UUID userId);

    /** @throws com.financialplanner.modulecommonbc.exception.ItemNotFoundException missing, or someone else's */
    CreditAccount get(Long id, UUID requesterUserId, boolean requesterIsAdmin);

    CreditAccount create(CreditAccountDraft draft, UUID ownerUserId);

    /** Changes the account's fields and payments (replaced as a set). Purchases are managed separately. */
    CreditAccount update(Long id, CreditAccountDraft draft, UUID requesterUserId, boolean requesterIsAdmin);

    /** Deletes the account with its purchases and payments. The paying items themselves are untouched. */
    void delete(Long id, UUID requesterUserId, boolean requesterIsAdmin);

    AccountPurchase addPurchase(Long accountId, PurchaseDraft draft, UUID requesterUserId, boolean requesterIsAdmin);

    AccountPurchase updatePurchase(Long accountId, Long purchaseId, PurchaseDraft draft, UUID requesterUserId,
                                   boolean requesterIsAdmin);

    void deletePurchase(Long accountId, Long purchaseId, UUID requesterUserId, boolean requesterIsAdmin);
}
