package com.financialplanner.moduledisplaybc.accounts;

import com.financialplanner.moduledisplaybc.accounts.AccountLedgerModels.*;
import com.financialplanner.moduledisplaybc.paydown.PaydownModels.LedgerAdjustment;
import com.financialplanner.moduledisplaybc.recurrence.RecurrenceOccurrences;
import com.financialplanner.moduleitemsbc.domain.account.PurchaseRules;
import com.financialplanner.moduleitemsbc.domain.debt.DebtRules;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.AccountPayment;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.AccountPurchase;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.CreditAccount;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Runs each credit card or loan forward day by day. Pure (no database, no clock) so every rule is unit-tested.
 * Port of API-NET's {@code AccountLedgerCalculator}.
 * <ul>
 *     <li>The opening balance is what was owed at the end of its date; activity counts from the next day.</li>
 *     <li>Each occurrence of a purchase adds its amount. Purchases never touch the main bank forecast.</li>
 *     <li>Each occurrence of a paying credit sends its fixed amount, or its percentage of the credit (rounded to
 *     cents). What is actually paid leaves the bank, as a {@link LedgerAdjustment} on the main ledger.</li>
 *     <li>Each occurrence of a paying debit (e.g. the card's monthly payment) pays its whole amount. The debit is
 *     already on the main ledger, so it adds no adjustment.</li>
 *     <li>A payment never takes the balance below zero: it's cut to what's owed, and nothing is paid on a zero
 *     balance.</li>
 *     <li>Within a day, purchases come first (by name), then payments (by item name).</li>
 *     <li>Interest is APR / 12 on the balance at each month end (after that day's activity), while anything is
 *     owed.</li>
 * </ul>
 * The run starts at the balance date, so a window that starts later still begins from the right balance, and
 * keeps going past the window (up to {@link #HORIZON_YEARS}) to find the payoff date.
 */
public class AccountLedgerCalculator {

    /** Item-type name shown on the main ledger for money sent to a card or loan. */
    public static final String PAYMENT_TYPE_NAME = "Card/Loan Payment";

    public static final int HORIZON_YEARS = 30;

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal TWELVE = new BigDecimal("12");
    private static final MathContext RATE = MathContext.DECIMAL128;

    private final RecurrenceOccurrences occurrences;

    public AccountLedgerCalculator(RecurrenceOccurrences occurrences) {
        this.occurrences = occurrences;
    }

    public AccountLedgerResult calculate(Collection<Item> items, Collection<CreditAccount> accounts,
                                         LocalDate start, LocalDate end) {
        Map<Long, Item> byId = items.stream()
                                    .filter(i -> i.getId() != null)
                                    .collect(Collectors.toMap(Item::getId, Function.identity(), (a, b) -> a));

        List<CreditAccount> ordered = new ArrayList<>(accounts);
        ordered.sort(Comparator.comparing(CreditAccount::getName, Comparator.nullsLast(Comparator.naturalOrder()))
                               .thenComparing(CreditAccount::getId, Comparator.nullsLast(Comparator.naturalOrder())));

        List<AccountLedger> results = new ArrayList<>();
        List<LedgerAdjustment> adjustments = new ArrayList<>();
        for (CreditAccount account : ordered) {
            run(account, byId, start, end, results, adjustments);
        }

        adjustments.sort(Comparator.comparing(LedgerAdjustment::date).thenComparing(LedgerAdjustment::name));
        return new AccountLedgerResult(results, adjustments);
    }

    private record Scheduled(LocalDate date, int order, String sortName, Long sourceId, AccountEventKind kind,
                             BigDecimal amount, BigDecimal percent, boolean fromDebit) {}

    private void run(CreditAccount account, Map<Long, Item> items, LocalDate start, LocalDate end,
                     List<AccountLedger> results, List<LedgerAdjustment> adjustments) {
        LocalDate balanceDate = account.getBalanceDate();
        LocalDate first = balanceDate.plusDays(1);
        LocalDate horizon = balanceDate.plusYears(HORIZON_YEARS);
        LocalDate last = end.isAfter(horizon) ? end : horizon;

        List<Scheduled> scheduled = new ArrayList<>();

        for (AccountPurchase purchase : account.getPurchases()) {
            BigDecimal amount = money(purchase.getAmount());
            if (amount.signum() <= 0) {
                continue;
            }
            for (LocalDate date : occurrences.of(PurchaseRules.asScheduleItem(purchase), first, last)) {
                scheduled.add(new Scheduled(date, 0, nameOf(purchase.getName()), purchase.getId(),
                                            AccountEventKind.PURCHASE, amount, null, false));
            }
        }

        for (AccountPayment payment : account.getPayments()) {
            Item source = payment.getCreditItem() == null ? null : items.get(payment.getCreditItem().getId());
            long type = DebtRules.typeOf(source);
            if (source == null || (type != DebtRules.CREDIT && type != DebtRules.DEBIT)) {
                continue;
            }

            boolean fromDebit = type == DebtRules.DEBIT;
            BigDecimal amount;
            if (fromDebit) {
                amount = money(source.getAmount());
            } else if (payment.getPercent() != null) {
                amount = money(source.getAmount()).multiply(payment.getPercent().min(HUNDRED))
                                                  .divide(HUNDRED, 2, RoundingMode.HALF_UP);
            } else {
                amount = money(payment.getAmount());
            }

            if (amount.signum() > 0) {
                for (LocalDate date : occurrences.of(source, first, last)) {
                    scheduled.add(new Scheduled(date, 1, nameOf(source.getName()), source.getId(),
                                                AccountEventKind.PAYMENT, amount,
                                                fromDebit ? null : payment.getPercent(), fromDebit));
                }
            }
        }

        Map<LocalDate, List<Scheduled>> byDate = scheduled.stream()
            .sorted(Comparator.comparing(Scheduled::date)
                              .thenComparingInt(Scheduled::order)
                              .thenComparing(Scheduled::sortName)
                              .thenComparing(Scheduled::sourceId, Comparator.nullsFirst(Comparator.naturalOrder())))
            .collect(Collectors.groupingBy(Scheduled::date, LinkedHashMap::new, Collectors.toList()));

        BigDecimal apr = account.getApr() == null ? BigDecimal.ZERO : account.getApr();
        BigDecimal monthlyRate = apr.divide(HUNDRED, RATE).divide(TWELVE, RATE);
        BigDecimal opening = account.getOpeningBalance() == null ? BigDecimal.ZERO : account.getOpeningBalance();
        BigDecimal balance = opening.max(BigDecimal.ZERO);
        LocalDate payoff = balance.signum() == 0 ? balanceDate : null;

        int windowDays = (int) ChronoUnit.DAYS.between(start, end) + 1;
        List<AccountEvent> events = new ArrayList<>();
        List<LedgerAdjustment> accountAdjustments = new ArrayList<>();
        List<BigDecimal> balances = new ArrayList<>(Math.max(windowDays, 0));

        // Days of the window on or before the balance date have no history: show the opening balance.
        for (LocalDate day = start; !day.isAfter(end) && !day.isAfter(balanceDate); day = day.plusDays(1)) {
            balances.add(balance);
        }

        BigDecimal startingBalance = balance;

        for (LocalDate day = first; !day.isAfter(last); day = day.plusDays(1)) {
            boolean inWindow = !day.isBefore(start) && !day.isAfter(end);
            if (day.equals(start)) {
                startingBalance = balance;
            }

            for (Scheduled entry : byDate.getOrDefault(day, List.of())) {
                BigDecimal change;
                if (entry.kind() == AccountEventKind.PURCHASE) {
                    change = entry.amount();
                } else {
                    BigDecimal paid = entry.amount().min(balance);
                    if (paid.signum() <= 0) {
                        continue;
                    }
                    change = paid.negate();
                    if (inWindow && !entry.fromDebit()) {
                        String detail = entry.percent() != null
                                        ? DebtRules.format(entry.percent()) + "% of " + entry.sortName()
                                        : "from " + entry.sortName();
                        accountAdjustments.add(new LedgerAdjustment(day, entry.sourceId(),
                                                                    account.getName() + " payment", detail,
                                                                    paid.negate()));
                    }
                }

                balance = balance.add(change);
                if (balance.signum() == 0 && change.signum() < 0) {
                    if (payoff == null) {
                        payoff = day;
                    }
                } else if (balance.signum() > 0) {
                    payoff = null; // a later purchase reopened the balance
                }

                if (inWindow) {
                    events.add(new AccountEvent(day, entry.kind(), entry.sourceId(), entry.sortName(),
                                                entry.percent(), change, balance));
                }
            }

            if (balance.signum() > 0 && monthlyRate.signum() > 0 && day.getDayOfMonth() == day.lengthOfMonth()) {
                BigDecimal interest = balance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
                if (interest.signum() > 0) {
                    balance = balance.add(interest);
                    if (inWindow) {
                        events.add(new AccountEvent(day, AccountEventKind.INTEREST, null, null, null, interest,
                                                    balance));
                    }
                }
            }

            if (inWindow) {
                balances.add(balance);
            }

            // Past the window, keep going only to find the payoff date.
            if (!day.isBefore(end) && balance.signum() == 0) {
                break;
            }
        }

        // A window that ends after a payoff that stopped the loop early stays at zero.
        while (balances.size() < windowDays) {
            balances.add(BigDecimal.ZERO);
        }

        BigDecimal totalPurchases = sum(events, AccountEventKind.PURCHASE);
        BigDecimal totalPaid = sum(events, AccountEventKind.PAYMENT).negate();
        BigDecimal totalInterest = sum(events, AccountEventKind.INTEREST);

        results.add(new AccountLedger(account.getId(), account.getName(), account.getKind(), apr,
                                      account.getCreditLimit(), opening, balanceDate, startingBalance,
                                      balances.isEmpty() ? startingBalance : balances.getLast(), balances,
                                      totalPurchases, totalPaid, totalInterest,
                                      balance.signum() == 0 ? payoff : null, events));
        adjustments.addAll(accountAdjustments);
    }

    private static BigDecimal sum(List<AccountEvent> events, AccountEventKind kind) {
        return events.stream()
                     .filter(e -> e.kind() == kind)
                     .map(AccountEvent::amount)
                     .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static String nameOf(String name) {
        return name == null ? "" : name;
    }

    /** Positive amounts rounded to cents; anything else is 0. */
    private static BigDecimal money(BigDecimal amount) {
        return amount != null && amount.signum() > 0 ? amount.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    /** Positive, finite amounts rounded to cents; anything else is 0. */
    private static BigDecimal money(Double amount) {
        return amount != null && Double.isFinite(amount) && amount > 0
               ? BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP)
               : BigDecimal.ZERO;
    }
}
