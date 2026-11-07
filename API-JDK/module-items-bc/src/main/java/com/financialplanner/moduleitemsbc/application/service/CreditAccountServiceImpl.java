package com.financialplanner.moduleitemsbc.application.service;

import com.financialplanner.modulecommonbc.exception.DomainValidationException;
import com.financialplanner.modulecommonbc.exception.ItemNotFoundException;
import com.financialplanner.moduleitemsbc.domain.account.CreditAccountDraft;
import com.financialplanner.moduleitemsbc.domain.account.CreditAccountRules;
import com.financialplanner.moduleitemsbc.domain.account.PurchaseDraft;
import com.financialplanner.moduleitemsbc.domain.account.PurchaseRules;
import com.financialplanner.moduleitemsbc.domain.repository.CreditAccountRepository;
import com.financialplanner.moduleitemsbc.domain.repository.ItemRepository;
import com.financialplanner.moduleitemsbc.domain.repository.TimePeriodRepository;
import com.financialplanner.moduleitemsbc.domain.service.CreditAccountService;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Cards & Loans. As with items and debts, a user only ever sees and changes their own (someone else's answers
 * 404), the owner comes from the access token, and every save goes through {@link CreditAccountRules} or
 * {@link PurchaseRules}. Port of API-NET's {@code CreditAccountService}.
 */
@Service
public class CreditAccountServiceImpl implements CreditAccountService {

    private final CreditAccountRepository accounts;
    private final ItemRepository items;
    private final TimePeriodRepository timePeriods;
    private final Clock clock;

    @Autowired
    public CreditAccountServiceImpl(CreditAccountRepository accounts, ItemRepository items,
                                    TimePeriodRepository timePeriods) {
        this(accounts, items, timePeriods, Clock.systemUTC());
    }

    /** For tests: a fixed clock decides "today". */
    public CreditAccountServiceImpl(CreditAccountRepository accounts, ItemRepository items,
                                    TimePeriodRepository timePeriods, Clock clock) {
        this.accounts    = accounts;
        this.items       = items;
        this.timePeriods = timePeriods;
        this.clock       = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreditAccount> list(UUID userId) {
        if (userId == null) {
            throw new DomainValidationException("UserId cannot be null");
        }
        List<CreditAccount> list = accounts.findByUserId(userId);
        return list == null ? List.of() : list;
    }

    @Override
    @Transactional(readOnly = true)
    public CreditAccount get(Long id, UUID requesterUserId, boolean requesterIsAdmin) {
        if (id == null || id <= 0) {
            throw new DomainValidationException("Account id must be a positive integer");
        }
        CreditAccount account = accounts.findById(id).orElse(null);
        if (account == null || (!requesterIsAdmin && !account.getUserId().equals(requesterUserId))) {
            throw new ItemNotFoundException("Account " + id + " not found");
        }
        return account;
    }

    @Override
    @Transactional
    public CreditAccount create(CreditAccountDraft draft, UUID ownerUserId) {
        Map<Long, Item> ownerItems = ownerItems(ownerUserId);
        CreditAccountRules.Normalized values =
            CreditAccountRules.normalize(draft, ownerItems, list(ownerUserId), today());

        CreditAccount account = new CreditAccount();
        account.setUserId(ownerUserId);
        apply(account, values);
        return accounts.save(account);
    }

    @Override
    @Transactional
    public CreditAccount update(Long id, CreditAccountDraft draft, UUID requesterUserId, boolean requesterIsAdmin) {
        CreditAccount account = get(id, requesterUserId, requesterIsAdmin);
        Map<Long, Item> ownerItems = ownerItems(account.getUserId());
        List<CreditAccount> others = list(account.getUserId()).stream().filter(a -> !a.getId().equals(id)).toList();
        CreditAccountRules.Normalized values = CreditAccountRules.normalize(draft, ownerItems, others, today());

        apply(account, values);
        return accounts.save(account);
    }

    @Override
    @Transactional
    public void delete(Long id, UUID requesterUserId, boolean requesterIsAdmin) {
        get(id, requesterUserId, requesterIsAdmin);
        accounts.deleteById(id);
    }

    @Override
    @Transactional
    public AccountPurchase addPurchase(Long accountId, PurchaseDraft draft, UUID requesterUserId,
                                       boolean requesterIsAdmin) {
        CreditAccount account = get(accountId, requesterUserId, requesterIsAdmin);
        Set<Long> before = new HashSet<>();
        for (AccountPurchase p : account.getPurchases()) {
            before.add(p.getId());
        }

        AccountPurchase purchase = new AccountPurchase();
        purchase.setAccount(account);
        purchase.setTimePeriod(timePeriod(PurchaseRules.normalize(draft, purchase)));
        account.getPurchases().add(purchase);

        // Saving a loaded account merges it, which may persist a copy of the new purchase rather than this
        // instance; the saved account's purchase with an id not seen before is the one just added.
        CreditAccount saved = accounts.save(account);
        return saved.getPurchases().stream()
                    .filter(p -> p.getId() != null && !before.contains(p.getId()))
                    .findFirst()
                    .orElseThrow(() -> new ItemNotFoundException("Purchase not found after saving"));
    }

    @Override
    @Transactional
    public AccountPurchase updatePurchase(Long accountId, Long purchaseId, PurchaseDraft draft, UUID requesterUserId,
                                          boolean requesterIsAdmin) {
        CreditAccount account = get(accountId, requesterUserId, requesterIsAdmin);
        AccountPurchase purchase = findPurchase(account, purchaseId);

        // Check on a scratch copy first, so a rejected draft leaves the saved purchase untouched.
        AccountPurchase checked = new AccountPurchase();
        long period = PurchaseRules.normalize(draft, checked);

        purchase.setName(checked.getName());
        purchase.setAmount(checked.getAmount());
        purchase.setBeginDate(checked.getBeginDate());
        purchase.setEndDate(checked.getEndDate());
        purchase.setWeeklyDow(checked.getWeeklyDow());
        purchase.setDateRangeReq(checked.getDateRangeReq());
        purchase.setTimePeriod(timePeriod(period));

        return findPurchase(accounts.save(account), purchaseId);
    }

    @Override
    @Transactional
    public void deletePurchase(Long accountId, Long purchaseId, UUID requesterUserId, boolean requesterIsAdmin) {
        CreditAccount account = get(accountId, requesterUserId, requesterIsAdmin);
        AccountPurchase purchase = findPurchase(account, purchaseId);
        account.getPurchases().remove(purchase);
        accounts.save(account);
    }

    /**
     * Copies the checked values onto the account. Payments from an item that still pays are updated in place
     * rather than deleted and re-inserted, so the (account, item) unique key is never hit mid-flush.
     */
    static void apply(CreditAccount account, CreditAccountRules.Normalized values) {
        account.setName(values.name());
        account.setKind(values.kind());
        account.setOpeningBalance(values.openingBalance());
        account.setBalanceDate(values.balanceDate());
        account.setApr(values.apr());
        account.setCreditLimit(values.creditLimit());

        Map<Long, AccountPayment> current = new HashMap<>();
        for (AccountPayment p : account.getPayments()) {
            current.put(p.getCreditItem().getId(), p);
        }

        List<AccountPayment> wanted = new ArrayList<>();
        for (CreditAccountRules.NormalizedPayment n : values.payments()) {
            AccountPayment payment = current.remove(n.source().getId());
            if (payment == null) {
                payment = new AccountPayment();
                payment.setAccount(account);
                payment.setCreditItem(n.source());
            }
            payment.setAmount(n.amount());
            payment.setPercent(n.percent());
            wanted.add(payment);
        }

        account.getPayments().removeIf(p -> wanted.stream().noneMatch(w -> w == p));
        for (AccountPayment p : wanted) {
            if (account.getPayments().stream().noneMatch(existing -> existing == p)) {
                account.getPayments().add(p);
            }
        }
    }

    private static AccountPurchase findPurchase(CreditAccount account, Long purchaseId) {
        return account.getPurchases().stream()
                      .filter(p -> p.getId() != null && p.getId().equals(purchaseId))
                      .findFirst()
                      .orElseThrow(() -> new ItemNotFoundException("Purchase " + purchaseId + " not found"));
    }

    private TimePeriod timePeriod(long id) {
        return timePeriods.findById(id)
                          .orElseThrow(() -> new DomainValidationException("Time period " + id + " not found"));
    }

    private Map<Long, Item> ownerItems(UUID userId) {
        if (userId == null) {
            throw new DomainValidationException("UserId cannot be null");
        }
        List<Item> list = items.findByUserId(userId);
        return (list == null ? List.<Item>of() : list).stream()
                                                      .collect(Collectors.toMap(Item::getId, Function.identity(), (a, b) -> a));
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }
}
