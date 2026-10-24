package com.financialplanner.moduleitemsbc.domain.debt;

import com.financialplanner.modulecommonbc.exception.DomainValidationException;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Debt;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.DebtAllocation;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * The rules a tracked debt and its allocations must satisfy, in one place (same rules as API-NET's
 * {@code DebtRules}). Pure: no database, no clock.
 */
public final class DebtRules {

    public static final long CREDIT = 1L;
    public static final long DEBIT = 2L;

    public static final BigDecimal MAX_BALANCE = new BigDecimal("1000000000");
    public static final BigDecimal MAX_APR = new BigDecimal("100");
    public static final BigDecimal HUNDRED = new BigDecimal("100");
    public static final int MAX_ALLOCATIONS = 20;

    private DebtRules() {}

    /** The checked values to save. */
    public record Normalized(BigDecimal balance, BigDecimal apr, LocalDate balanceDate,
                             List<NormalizedAllocation> allocations) {}

    public record NormalizedAllocation(Item credit, BigDecimal percent) {}

    /**
     * Checks a draft against the owner's items and their other debts.
     *
     * @param draft      the request
     * @param debitItem  the debit being tracked (already checked to be the owner's)
     * @param ownerItems every item the owner has, by id
     * @param otherDebts the owner's other debts (not this one), for the per-credit 100% cap
     * @param today      used when the draft has no balance date
     * @throws DomainValidationException when any rule is broken; the message says which
     */
    public static Normalized normalize(DebtDraft draft, Item debitItem, Map<Long, Item> ownerItems,
                                       Collection<Debt> otherDebts, LocalDate today) {
        if (typeOf(debitItem) != DEBIT) {
            throw invalid("Only a debit can be tracked as a debt");
        }

        BigDecimal balance = draft.balance();
        if (balance == null || balance.signum() < 0 || balance.compareTo(MAX_BALANCE) > 0) {
            throw invalid("Balance owed is required and must be between $0 and $1,000,000,000");
        }

        BigDecimal apr = draft.apr() == null ? BigDecimal.ZERO : draft.apr();
        if (apr.signum() < 0 || apr.compareTo(MAX_APR) > 0) {
            throw invalid("APR must be between 0% and 100%");
        }

        List<DebtDraft.AllocationDraft> allocations = draft.allocations() == null ? List.of() : draft.allocations();
        if (allocations.size() > MAX_ALLOCATIONS) {
            throw invalid("A debt can have at most " + MAX_ALLOCATIONS + " allocations");
        }

        // How much of each credit the owner's other debts already take.
        Map<Long, BigDecimal> taken = new HashMap<>();
        for (Debt other : otherDebts) {
            for (DebtAllocation a : other.getAllocations()) {
                taken.merge(a.getCreditItem().getId(), a.getPercent(), BigDecimal::add);
            }
        }

        List<NormalizedAllocation> result = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        for (DebtDraft.AllocationDraft allocation : allocations) {
            Long creditId = allocation == null ? null : allocation.creditItemId();
            Item credit = creditId == null ? null : ownerItems.get(creditId);
            if (credit == null) {
                throw invalid("Each allocation needs one of your credits");
            }
            if (typeOf(credit) != CREDIT) {
                throw invalid("\"" + credit.getName() + "\" is not a credit");
            }
            if (!seen.add(creditId)) {
                throw invalid("\"" + credit.getName() + "\" is allocated to this debt more than once");
            }

            BigDecimal raw = allocation.percent();
            if (raw == null || raw.signum() <= 0 || raw.compareTo(HUNDRED) > 0) {
                throw invalid("The percentage of \"" + credit.getName() + "\" must be more than 0% and at most 100%");
            }

            BigDecimal percent = raw.setScale(2, RoundingMode.HALF_UP);
            BigDecimal already = taken.getOrDefault(creditId, BigDecimal.ZERO);
            if (already.add(percent).compareTo(HUNDRED) > 0) {
                throw invalid("\"" + credit.getName() + "\" is already " + format(already)
                              + "% allocated to other debts; at most " + format(HUNDRED.subtract(already)) + "% is left");
            }

            result.add(new NormalizedAllocation(credit, percent));
        }

        return new Normalized(balance.setScale(2, RoundingMode.HALF_UP), apr.setScale(3, RoundingMode.HALF_UP),
                              draft.balanceDate() != null ? draft.balanceDate() : today, result);
    }

    /** 1 = credit, 2 = debit, 3 = initial amount; 0 when unknown. */
    public static long typeOf(Item item) {
        return item != null && item.getItemType() != null && item.getItemType().getId() != null
               ? item.getItemType().getId() : 0L;
    }

    /** 25.00 → "25", 12.50 → "12.5". */
    public static String format(BigDecimal value) {
        BigDecimal v = value.stripTrailingZeros();
        return (v.scale() < 0 ? v.setScale(0) : v).toPlainString();
    }

    private static DomainValidationException invalid(String message) {
        return new DomainValidationException(message);
    }
}
