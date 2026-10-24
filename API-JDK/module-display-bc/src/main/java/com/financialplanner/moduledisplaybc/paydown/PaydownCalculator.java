package com.financialplanner.moduledisplaybc.paydown;

import com.financialplanner.moduledisplaybc.paydown.PaydownModels.*;
import com.financialplanner.moduledisplaybc.recurrence.RecurrenceOccurrences;
import com.financialplanner.moduleitemsbc.domain.debt.DebtRules;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Debt;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.DebtAllocation;
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
 * Projects how each tracked debt is paid down. Pure (no database, no clock) so every rule is unit-tested.
 * Port of API-NET's {@code PaydownCalculator}.
 * <ul>
 *     <li>The entered balance is what was owed at the end of its date; activity counts from the next day.</li>
 *     <li>Each occurrence of the debt's own debit pays its amount; each occurrence of an allocated credit pays
 *     its percentage of the credit (rounded to cents).</li>
 *     <li>Payments never go below zero: the last one is cut to what's left, and none follow the payoff.</li>
 *     <li>Interest is APR / 12 on the balance at each month end (after that day's payments), while anything is
 *     owed.</li>
 *     <li>Within a day the debit's payment comes first, then allocations by credit name.</li>
 * </ul>
 * The simulation runs from the balance date, so a window that starts later still begins from the right
 * balance, and keeps going past the window (up to {@link #HORIZON_YEARS}) to find the payoff date.
 * <p>
 * The debit's own payment is already a debit on the main ledger, so only allocations become
 * {@link LedgerAdjustment}s.
 */
public class PaydownCalculator {

    public static final int HORIZON_YEARS = 30;

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal TWELVE = new BigDecimal("12");
    private static final MathContext RATE = MathContext.DECIMAL128;

    private final RecurrenceOccurrences occurrences;

    public PaydownCalculator(RecurrenceOccurrences occurrences) {
        this.occurrences = occurrences;
    }

    public PaydownResult calculate(Collection<Item> items, Collection<Debt> debts, LocalDate start, LocalDate end) {
        Map<Long, Item> byId = items.stream()
                                    .filter(i -> i.getId() != null)
                                    .collect(Collectors.toMap(Item::getId, Function.identity(), (a, b) -> a));

        List<Debt> ordered = new ArrayList<>(debts);
        ordered.sort(Comparator.comparing((Debt d) -> nameOf(byId.get(itemIdOf(d))), Comparator.nullsLast(Comparator.naturalOrder()))
                               .thenComparing(Debt::getId, Comparator.nullsLast(Comparator.naturalOrder())));

        List<DebtPaydown> results = new ArrayList<>();
        List<LedgerAdjustment> adjustments = new ArrayList<>();

        for (Debt debt : ordered) {
            Item debit = byId.get(itemIdOf(debt));
            if (debit == null) {
                continue; // the debit is gone; nothing to project
            }
            project(debt, debit, byId, start, end, results, adjustments);
        }

        adjustments.sort(Comparator.comparing(LedgerAdjustment::date).thenComparing(LedgerAdjustment::name));
        return new PaydownResult(results, adjustments);
    }

    private record Scheduled(LocalDate date, int order, String sortName, EventKind kind, Item source,
                             BigDecimal amount, BigDecimal percent) {}

    private void project(Debt debt, Item debit, Map<Long, Item> items, LocalDate start, LocalDate end,
                         List<DebtPaydown> results, List<LedgerAdjustment> adjustments) {
        LocalDate balanceDate = debt.getBalanceDate();
        LocalDate first = balanceDate.plusDays(1);
        LocalDate horizon = balanceDate.plusYears(HORIZON_YEARS);
        LocalDate last = end.isAfter(horizon) ? end : horizon;

        List<Scheduled> scheduled = new ArrayList<>();
        BigDecimal payment = money(debit.getAmount());
        if (payment.signum() > 0) {
            for (LocalDate date : occurrences.of(debit, first, last)) {
                scheduled.add(new Scheduled(date, 0, null, EventKind.PAYMENT, debit, payment, null));
            }
        }

        for (DebtAllocation allocation : debt.getAllocations()) {
            Item credit = allocation.getCreditItem() == null ? null : items.get(allocation.getCreditItem().getId());
            BigDecimal percent = allocation.getPercent();
            if (credit == null || percent == null || percent.signum() <= 0) {
                continue;
            }
            BigDecimal share = money(credit.getAmount()).multiply(percent.min(HUNDRED))
                                                        .divide(HUNDRED, 2, RoundingMode.HALF_UP);
            if (share.signum() > 0) {
                for (LocalDate date : occurrences.of(credit, first, last)) {
                    scheduled.add(new Scheduled(date, 1, credit.getName(), EventKind.ALLOCATION, credit, share, percent));
                }
            }
        }

        Map<LocalDate, List<Scheduled>> byDate = scheduled.stream()
            .sorted(Comparator.comparing(Scheduled::date)
                              .thenComparingInt(Scheduled::order)
                              .thenComparing(Scheduled::sortName, Comparator.nullsFirst(Comparator.naturalOrder()))
                              .thenComparing(s -> s.source().getId()))
            .collect(Collectors.groupingBy(Scheduled::date, LinkedHashMap::new, Collectors.toList()));

        String name = debit.getName();
        BigDecimal apr = debt.getApr() == null ? BigDecimal.ZERO : debt.getApr();
        BigDecimal monthlyRate = apr.divide(HUNDRED, RATE).divide(TWELVE, RATE);
        BigDecimal balance = debt.getBalance() == null ? BigDecimal.ZERO : debt.getBalance().max(BigDecimal.ZERO);
        LocalDate payoff = balance.signum() == 0 ? balanceDate : null;

        int windowDays = (int) ChronoUnit.DAYS.between(start, end) + 1;
        List<PaydownEvent> events = new ArrayList<>();
        List<LedgerAdjustment> debtAdjustments = new ArrayList<>();
        List<BigDecimal> balances = new ArrayList<>(Math.max(windowDays, 0));

        // Days of the window up to the balance date have no history: show the entered balance.
        for (LocalDate day = start; !day.isAfter(end) && !day.isAfter(balanceDate); day = day.plusDays(1)) {
            balances.add(balance);
        }

        BigDecimal startingBalance = balance;

        for (LocalDate day = first; !day.isAfter(last); day = day.plusDays(1)) {
            boolean inWindow = !day.isBefore(start) && !day.isAfter(end);
            if (day.equals(start)) {
                startingBalance = balance;
            }

            for (Scheduled item : byDate.getOrDefault(day, List.of())) {
                if (balance.signum() == 0) {
                    break;
                }

                BigDecimal paid = item.amount().min(balance);
                balance = balance.subtract(paid);
                if (balance.signum() == 0 && payoff == null) {
                    payoff = day;
                }

                if (inWindow) {
                    events.add(new PaydownEvent(day, item.kind(), item.source().getId(), item.source().getName(),
                                                item.percent(), paid.negate(), balance));

                    if (item.kind() == EventKind.ALLOCATION) {
                        debtAdjustments.add(new LedgerAdjustment(day, debit.getId(), name + " paydown",
                                                                 DebtRules.format(item.percent()) + "% of " + item.source().getName(),
                                                                 paid.negate()));
                    }
                }
            }

            if (balance.signum() > 0 && monthlyRate.signum() > 0 && day.getDayOfMonth() == day.lengthOfMonth()) {
                BigDecimal interest = balance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
                if (interest.signum() > 0) {
                    balance = balance.add(interest);
                    if (inWindow) {
                        events.add(new PaydownEvent(day, EventKind.INTEREST, null, null, null, interest, balance));
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

        // A payoff that stopped the loop early leaves the rest of the window at zero.
        while (balances.size() < windowDays) {
            balances.add(BigDecimal.ZERO);
        }

        BigDecimal totalPaid = events.stream()
                                     .filter(e -> e.kind() != EventKind.INTEREST)
                                     .map(PaydownEvent::amount)
                                     .reduce(BigDecimal.ZERO, BigDecimal::add)
                                     .negate();
        BigDecimal totalInterest = events.stream()
                                         .filter(e -> e.kind() == EventKind.INTEREST)
                                         .map(PaydownEvent::amount)
                                         .reduce(BigDecimal.ZERO, BigDecimal::add);

        results.add(new DebtPaydown(debt.getId(), debit.getId(), name, apr, debt.getBalance(), balanceDate,
                                    startingBalance, balances.isEmpty() ? startingBalance : balances.getLast(),
                                    balances, totalPaid, totalInterest, payoff, events));
        adjustments.addAll(debtAdjustments);
    }

    private static Long itemIdOf(Debt debt) {
        return debt.getItem() == null ? null : debt.getItem().getId();
    }

    private static String nameOf(Item item) {
        return item == null ? null : item.getName();
    }

    /** Positive, finite amounts rounded to cents; anything else is 0. */
    private static BigDecimal money(Double amount) {
        return amount != null && Double.isFinite(amount) && amount > 0
               ? BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP)
               : BigDecimal.ZERO;
    }
}
