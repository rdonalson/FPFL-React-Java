package com.financialplanner.moduleitemsbc.application.service;

import com.financialplanner.modulecommonbc.exception.DomainValidationException;
import com.financialplanner.modulecommonbc.exception.DuplicateItemException;
import com.financialplanner.modulecommonbc.exception.ItemNotFoundException;
import com.financialplanner.moduleitemsbc.domain.debt.DebtDraft;
import com.financialplanner.moduleitemsbc.domain.debt.DebtRules;
import com.financialplanner.moduleitemsbc.domain.repository.DebtRepository;
import com.financialplanner.moduleitemsbc.domain.repository.ItemRepository;
import com.financialplanner.moduleitemsbc.domain.service.DebtService;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Debt;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.DebtAllocation;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Debt Paydown's tracked debts. The owner comes from the access token; every save goes through
 * {@link DebtRules}, including the rule that a credit can't be more than 100% allocated across all of the
 * user's debts.
 */
@Service
public class DebtServiceImpl implements DebtService {

    private final DebtRepository debts;
    private final ItemRepository items;
    private final Clock clock;

    @org.springframework.beans.factory.annotation.Autowired
    public DebtServiceImpl(DebtRepository debts, ItemRepository items) {
        this(debts, items, Clock.systemUTC());
    }

    /** For tests: a fixed clock decides "today". */
    public DebtServiceImpl(DebtRepository debts, ItemRepository items, Clock clock) {
        this.debts = debts;
        this.items = items;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Debt> list(UUID userId) {
        if (userId == null) {
            throw new DomainValidationException("UserId cannot be null");
        }
        List<Debt> list = debts.findByUserId(userId);
        return list == null ? List.of() : list;
    }

    @Override
    @Transactional(readOnly = true)
    public Debt get(Long id, UUID requesterUserId, boolean requesterIsAdmin) {
        if (id == null || id <= 0) {
            throw new DomainValidationException("Debt id must be a positive integer");
        }
        Debt debt = debts.findById(id).orElse(null);
        if (debt == null || (!requesterIsAdmin && !debt.getUserId().equals(requesterUserId))) {
            throw new ItemNotFoundException("Debt " + id + " not found");
        }
        return debt;
    }

    @Override
    @Transactional
    public Debt create(DebtDraft draft, UUID ownerUserId) {
        Map<Long, Item> ownerItems = ownerItems(ownerUserId);
        Item debit = draft.itemId() == null ? null : ownerItems.get(draft.itemId());
        if (debit == null) {
            throw new DomainValidationException("Choose one of your debits to track");
        }

        List<Debt> existing = list(ownerUserId);
        if (existing.stream().anyMatch(d -> d.getItem().getId().equals(debit.getId()))) {
            throw new DuplicateItemException("\"" + debit.getName() + "\" is already being tracked");
        }

        DebtRules.Normalized values = DebtRules.normalize(draft, debit, ownerItems, existing, today());

        Debt debt = new Debt();
        debt.setUserId(ownerUserId);
        debt.setItem(debit);
        apply(debt, values);
        return debts.save(debt);
    }

    @Override
    @Transactional
    public Debt update(Long id, DebtDraft draft, UUID requesterUserId, boolean requesterIsAdmin) {
        Debt debt = get(id, requesterUserId, requesterIsAdmin);
        Map<Long, Item> ownerItems = ownerItems(debt.getUserId());
        Item debit = ownerItems.get(debt.getItem().getId());
        if (debit == null) {
            throw new ItemNotFoundException("Debt " + id + " not found");
        }
        if (draft.itemId() != null && !draft.itemId().equals(debit.getId())) {
            throw new DomainValidationException(
                "A tracked debt's debit can't be changed; delete it and track the other debit instead");
        }

        List<Debt> others = list(debt.getUserId()).stream().filter(d -> !d.getId().equals(id)).toList();
        DebtRules.Normalized values = DebtRules.normalize(draft, debit, ownerItems, others, today());

        apply(debt, values);
        return debts.save(debt);
    }

    @Override
    @Transactional
    public void delete(Long id, UUID requesterUserId, boolean requesterIsAdmin) {
        get(id, requesterUserId, requesterIsAdmin);
        debts.deleteById(id);
    }

    /**
     * Copies the checked values onto the entity. Allocations for a credit that is still allocated are updated
     * in place rather than deleted and re-inserted, so the (debt, credit) unique key is never hit mid-flush.
     */
    static void apply(Debt debt, DebtRules.Normalized values) {
        debt.setBalance(values.balance());
        debt.setApr(values.apr());
        debt.setBalanceDate(values.balanceDate());

        Map<Long, DebtAllocation> current = new HashMap<>();
        for (DebtAllocation a : debt.getAllocations()) {
            current.put(a.getCreditItem().getId(), a);
        }

        List<DebtAllocation> wanted = new ArrayList<>();
        for (DebtRules.NormalizedAllocation n : values.allocations()) {
            DebtAllocation allocation = current.remove(n.credit().getId());
            if (allocation == null) {
                allocation = new DebtAllocation();
                allocation.setDebt(debt);
                allocation.setCreditItem(n.credit());
            }
            allocation.setPercent(n.percent());
            wanted.add(allocation);
        }

        debt.getAllocations().removeIf(a -> wanted.stream().noneMatch(w -> w == a));
        for (DebtAllocation a : wanted) {
            if (debt.getAllocations().stream().noneMatch(existing -> existing == a)) {
                debt.getAllocations().add(a);
            }
        }
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
