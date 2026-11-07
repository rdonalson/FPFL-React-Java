package com.financialplanner.moduleitemsbc.domain.account;

import com.financialplanner.modulecommonbc.exception.DomainValidationException;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.AccountPurchase;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemType;

import java.math.RoundingMode;
import java.util.Set;

/**
 * The rules a purchase must satisfy (same rules as API-NET's {@code PurchaseRules}). Purchases schedule like
 * items (same period ids and date-range meaning), limited for now to One Time, Daily and Weekly. Only the
 * fields belonging to the purchase's period are kept. Pure: no database.
 */
public final class PurchaseRules {

    public static final long ONE_TIME = 1L;
    public static final long DAILY = 2L;
    public static final long WEEKLY = 3L;
    public static final Set<Long> SUPPORTED_PERIODS = Set.of(ONE_TIME, DAILY, WEEKLY);

    private PurchaseRules() {}

    /**
     * Copies a checked draft onto {@code target} (a new purchase or the one being changed). The caller sets
     * the account and the {@code TimePeriod} entity for {@link AccountPurchase#getTimePeriod()}.
     *
     * @return the period id the purchase uses
     * @throws DomainValidationException when any rule is broken; the message says which
     */
    public static long normalize(PurchaseDraft draft, AccountPurchase target) {
        String name = draft.name() == null ? null : draft.name().trim();
        if (name == null || name.isEmpty()) {
            throw invalid("Purchase name is required and cannot be blank");
        }
        if (name.length() > CreditAccountRules.MAX_NAME_LENGTH) {
            throw invalid("Purchase name must be at most " + CreditAccountRules.MAX_NAME_LENGTH + " characters");
        }

        if (draft.amount() == null || draft.amount().signum() <= 0
            || draft.amount().compareTo(CreditAccountRules.MAX_MONEY) > 0) {
            throw invalid("Purchase amount is required and must be a positive currency value");
        }

        Long period = draft.timePeriodId();
        if (period == null || !SUPPORTED_PERIODS.contains(period)) {
            throw invalid("Purchases can be One Time (1), Daily (2) or Weekly (3)");
        }

        target.setName(name);
        target.setAmount(draft.amount().setScale(2, RoundingMode.HALF_UP));
        target.setWeeklyDow(null);
        target.setBeginDate(null);
        target.setEndDate(null);

        if (period == WEEKLY) {
            Integer dow = draft.weeklyDow();
            if (dow == null || dow < 0 || dow > 6) {
                throw invalid("Weekly day of week is required (0 = Sunday … 6 = Saturday)");
            }
            target.setWeeklyDow(dow);
        }

        if (period == ONE_TIME) {
            // A one-time purchase happens on its begin date; it has no end.
            if (draft.beginDate() == null) {
                throw invalid("A one-time purchase needs its date (beginDate)");
            }
            target.setBeginDate(draft.beginDate());
            target.setDateRangeReq(true);
            return period;
        }

        target.setDateRangeReq(draft.dateRangeReq());
        if (draft.dateRangeReq()) {
            if (draft.beginDate() == null || draft.endDate() == null) {
                throw invalid("Please select both begin and end dates for the date range");
            }
            if (draft.beginDate().isAfter(draft.endDate())) {
                throw invalid("Begin date must be before or equal to end date");
            }
            target.setBeginDate(draft.beginDate());
            target.setEndDate(draft.endDate());
        }
        return period;
    }

    /**
     * The purchase as a debit item, so the calculator can expand its dates with the same recurrence expanders as
     * everything else. Never saved.
     */
    public static Item asScheduleItem(AccountPurchase purchase) {
        Item item = new Item();
        item.setId(purchase.getId());
        item.setUserId(purchase.getAccount() == null ? null : purchase.getAccount().getUserId());
        item.setName(purchase.getName());
        item.setAmount(purchase.getAmount() == null ? 0.0 : purchase.getAmount().doubleValue());
        item.setItemType(new ItemType(2L, "Debit"));
        item.setTimePeriod(purchase.getTimePeriod());
        item.setBeginDate(purchase.getBeginDate());
        item.setEndDate(purchase.getEndDate());
        item.setWeeklyDow(purchase.getWeeklyDow());
        item.setDateRangeReq(Boolean.TRUE.equals(purchase.getDateRangeReq()));
        return item;
    }

    private static DomainValidationException invalid(String message) {
        return new DomainValidationException(message);
    }
}
