package com.financialplanner.moduleitemsbc.domain.account;

import com.financialplanner.modulecommonbc.exception.DomainValidationException;
import com.financialplanner.modulecommonbc.exception.DuplicateItemException;
import com.financialplanner.moduleitemsbc.domain.debt.DebtRules;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.AccountKind;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.AccountPayment;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.CreditAccount;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * The rules a card or loan and its payments must satisfy, in one place (same rules as API-NET's
 * {@code CreditAccountRules}). Pure: no database, no clock.
 */
public final class CreditAccountRules {

    public static final int MAX_NAME_LENGTH = 150;
    public static final BigDecimal MAX_MONEY = new BigDecimal("1000000000");
    public static final BigDecimal MAX_APR = new BigDecimal("100");
    public static final BigDecimal HUNDRED = new BigDecimal("100");
    public static final int MAX_PAYMENTS = 20;

    private CreditAccountRules() {}

    /** The account's fields once checked, plus its payments. */
    public record Normalized(String name, AccountKind kind, BigDecimal openingBalance, LocalDate balanceDate,
                             BigDecimal apr, BigDecimal creditLimit, List<NormalizedPayment> payments) {}

    /** {@code source} is a credit (with exactly one of amount / percent) or a debit (stored as 100%). */
    public record NormalizedPayment(Item source, BigDecimal amount, BigDecimal percent) {}

    /**
     * Checks a draft against the owner's items and their other accounts.
     *
     * @param draft         the request
     * @param ownerItems    every item the owner has, by id
     * @param otherAccounts the owner's other accounts (not this one), for unique names and the per-credit 100% cap
     * @param today         used when the draft has no balance date
     * @throws DomainValidationException when any rule is broken; the message says which
     * @throws DuplicateItemException    when the owner already has an account with that name
     */
    public static Normalized normalize(CreditAccountDraft draft, Map<Long, Item> ownerItems,
                                       Collection<CreditAccount> otherAccounts, LocalDate today) {
        List<CreditAccount> others = otherAccounts == null ? List.of() : List.copyOf(otherAccounts);

        String name = draft.name() == null ? null : draft.name().trim();
        if (name == null || name.isEmpty()) {
            throw invalid("Account name is required and cannot be blank");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw invalid("Account name must be at most " + MAX_NAME_LENGTH + " characters");
        }
        for (CreditAccount other : others) {
            if (other.getName() != null && other.getName().equalsIgnoreCase(name)) {
                throw new DuplicateItemException("You already have an account named \"" + name + "\"");
            }
        }

        AccountKind kind = parseKind(draft.kind());

        BigDecimal balance = draft.openingBalance();
        if (balance == null || balance.signum() < 0 || balance.compareTo(MAX_MONEY) > 0) {
            throw invalid("Opening balance is required and must be between $0 and $1,000,000,000");
        }

        BigDecimal apr = draft.apr() == null ? BigDecimal.ZERO : draft.apr();
        if (apr.signum() < 0 || apr.compareTo(MAX_APR) > 0) {
            throw invalid("APR must be between 0% and 100%");
        }

        BigDecimal limit = null;
        if (draft.creditLimit() != null) {
            if (kind != AccountKind.CreditCard) {
                throw invalid("Only a credit card has a credit limit");
            }
            if (draft.creditLimit().signum() <= 0 || draft.creditLimit().compareTo(MAX_MONEY) > 0) {
                throw invalid("Credit limit must be more than $0 and at most $1,000,000,000");
            }
            limit = money(draft.creditLimit());
        }

        return new Normalized(name, kind, money(balance),
                              draft.balanceDate() != null ? draft.balanceDate() : today,
                              apr.setScale(3, RoundingMode.HALF_UP), limit,
                              payments(draft.payments(), ownerItems, others));
    }

    /** {@code creditCard} or {@code loan}, any case; defaults to a credit card. */
    public static AccountKind parseKind(String kind) {
        String k = kind == null ? "" : kind.trim().toLowerCase(Locale.ROOT);
        return switch (k) {
            case "", "creditcard", "credit_card", "credit card" -> AccountKind.CreditCard;
            case "loan" -> AccountKind.Loan;
            default -> throw invalid("Kind must be \"creditCard\" or \"loan\"");
        };
    }

    private static List<NormalizedPayment> payments(List<CreditAccountDraft.PaymentDraft> drafts,
                                                    Map<Long, Item> ownerItems, List<CreditAccount> others) {
        List<CreditAccountDraft.PaymentDraft> payments = drafts == null ? List.of() : drafts;
        if (payments.size() > MAX_PAYMENTS) {
            throw invalid("An account can have at most " + MAX_PAYMENTS + " payments");
        }

        // How much of each item the owner's other accounts already take as a percentage.
        Map<Long, BigDecimal> taken = new HashMap<>();
        for (CreditAccount other : others) {
            for (AccountPayment p : other.getPayments()) {
                if (p.getPercent() != null && p.getCreditItem() != null) {
                    taken.merge(p.getCreditItem().getId(), p.getPercent(), BigDecimal::add);
                }
            }
        }

        List<NormalizedPayment> result = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        for (CreditAccountDraft.PaymentDraft payment : payments) {
            Long sourceId = payment == null ? null : payment.creditItemId();
            Item source = sourceId == null ? null : ownerItems.get(sourceId);
            if (source == null) {
                throw invalid("Each payment needs one of your credits or debits");
            }

            long type = DebtRules.typeOf(source);
            if (type != DebtRules.CREDIT && type != DebtRules.DEBIT) {
                throw invalid("\"" + source.getName() + "\" is not a credit or a debit");
            }
            if (!seen.add(sourceId)) {
                throw invalid("\"" + source.getName() + "\" pays this account more than once");
            }

            BigDecimal already = taken.getOrDefault(sourceId, BigDecimal.ZERO);

            if (type == DebtRules.DEBIT) {
                // A debit (e.g. the card's monthly payment) pays its whole amount; it's stored as 100%.
                boolean percentOk = payment.percent() == null || payment.percent().compareTo(HUNDRED) == 0;
                if (payment.amount() != null || !percentOk) {
                    throw invalid("\"" + source.getName()
                                  + "\" is a debit: it pays its whole amount, so leave amount and percentage empty");
                }
                if (already.signum() > 0) {
                    throw invalid("\"" + source.getName() + "\" already pays another account");
                }
                result.add(new NormalizedPayment(source, null, HUNDRED.setScale(2)));
                continue;
            }

            BigDecimal amount = payment.amount();
            BigDecimal rawPercent = payment.percent();
            if ((amount == null) == (rawPercent == null)) {
                throw invalid("The payment from \"" + source.getName()
                              + "\" needs either an amount or a percentage, not both");
            }

            if (amount != null) {
                BigDecimal creditAmount = itemAmount(source);
                if (amount.signum() <= 0 || amount.compareTo(creditAmount) > 0) {
                    throw invalid("The payment from \"" + source.getName()
                                  + "\" must be more than $0 and at most its amount ("
                                  + creditAmount.toPlainString() + ")");
                }
                result.add(new NormalizedPayment(source, money(amount), null));
                continue;
            }

            if (rawPercent.signum() <= 0 || rawPercent.compareTo(HUNDRED) > 0) {
                throw invalid("The percentage of \"" + source.getName() + "\" must be more than 0% and at most 100%");
            }
            BigDecimal percent = rawPercent.setScale(2, RoundingMode.HALF_UP);
            if (already.add(percent).compareTo(HUNDRED) > 0) {
                throw invalid("\"" + source.getName() + "\" already sends " + DebtRules.format(already)
                              + "% to other accounts; at most " + DebtRules.format(HUNDRED.subtract(already))
                              + "% is left");
            }
            result.add(new NormalizedPayment(source, null, percent));
        }
        return result;
    }

    /** The item's amount in cents; 0 when missing or not finite. */
    static BigDecimal itemAmount(Item item) {
        Double a = item.getAmount();
        return a != null && Double.isFinite(a) ? BigDecimal.valueOf(a).setScale(2, RoundingMode.HALF_UP)
                                               : BigDecimal.ZERO.setScale(2);
    }

    static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static DomainValidationException invalid(String message) {
        return new DomainValidationException(message);
    }
}
