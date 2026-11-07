package com.financialplanner.moduleapi.mappers;

import com.financialplanner.moduleapi.dtos.account.AccountLedgerResponse;
import com.financialplanner.moduleapi.dtos.account.AccountRequest;
import com.financialplanner.moduleapi.dtos.account.AccountResponse;
import com.financialplanner.moduleapi.dtos.account.PurchaseRequest;
import com.financialplanner.moduledisplaybc.accounts.AccountLedgerModels.AccountEvent;
import com.financialplanner.moduledisplaybc.accounts.AccountLedgerModels.AccountLedger;
import com.financialplanner.moduleitemsbc.domain.account.CreditAccountDraft;
import com.financialplanner.moduleitemsbc.domain.account.PurchaseDraft;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.*;
import com.financialplanner.modulecommonbc.sanitizer.Sanitizer;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Cards &amp; Loans mapping between requests, entities, calculator results and responses (same shapes as
 * API-NET's {@code AccountMapper}). Money leaves the API as plain numbers; outgoing data is sanitized by the
 * response factory.
 */
@Component
public class AccountMapper {

    private final Sanitizer sanitizer;

    public AccountMapper(Sanitizer sanitizer) {
        this.sanitizer = sanitizer;
    }

    public CreditAccountDraft toDraft(AccountRequest request) {
        sanitizer.sanitize(request);
        List<CreditAccountDraft.PaymentDraft> payments = new ArrayList<>();
        if (request.payments() != null) {
            for (AccountRequest.AccountPaymentRequest p : request.payments()) {
                payments.add(p == null ? new CreditAccountDraft.PaymentDraft(null, null, null)
                                       : new CreditAccountDraft.PaymentDraft(p.itemId(), decimal(p.amount()),
                                                                             decimal(p.percent())));
            }
        }
        return new CreditAccountDraft(request.name(), request.kind(), decimal(request.openingBalance()),
                                      request.balanceDate(), decimal(request.apr()), decimal(request.creditLimit()),
                                      payments);
    }

    public PurchaseDraft toDraft(PurchaseRequest request) {
        sanitizer.sanitize(request);
        return new PurchaseDraft(request.name(), decimal(request.amount()), request.fkPeriod(), request.beginDate(),
                                 request.endDate(), request.weeklyDow(), Boolean.TRUE.equals(request.dateRangeReq()));
    }

    /** {@code creditCard} or {@code loan}, as the UIs send and expect it. */
    public static String kindName(AccountKind kind) {
        return kind == AccountKind.Loan ? "loan" : "creditCard";
    }

    public AccountResponse toResponse(CreditAccount account) {
        List<AccountResponse.PurchaseResponse> purchases = new ArrayList<>(
            account.getPurchases().stream()
                   .sorted(Comparator.comparing(AccountPurchase::getBeginDate, Comparator.nullsFirst(Comparator.naturalOrder()))
                                     .thenComparing(AccountPurchase::getName, Comparator.nullsLast(Comparator.naturalOrder()))
                                     .thenComparing(AccountPurchase::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                   .map(AccountMapper::toResponse)
                   .toList());

        List<AccountResponse.AccountPaymentResponse> payments = new ArrayList<>(
            account.getPayments().stream()
                   .sorted(Comparator.comparing((AccountPayment p) -> p.getCreditItem() == null ? null : p.getCreditItem().getName(),
                                                Comparator.nullsLast(Comparator.naturalOrder())))
                   .map(AccountMapper::toResponse)
                   .toList());

        return new AccountResponse(account.getId(), account.getName(), kindName(account.getKind()),
                                   number(account.getOpeningBalance()), account.getBalanceDate(),
                                   number(account.getApr()), number(account.getCreditLimit()), purchases, payments);
    }

    public List<AccountResponse> toResponse(List<CreditAccount> accounts) {
        return new ArrayList<>(accounts.stream().map(this::toResponse).toList());
    }

    public static AccountResponse.PurchaseResponse toResponse(AccountPurchase purchase) {
        TimePeriod period = purchase.getTimePeriod();
        return new AccountResponse.PurchaseResponse(purchase.getId(), purchase.getAccountId(), purchase.getName(),
                                                    number(purchase.getAmount()),
                                                    period == null ? null : period.getId(),
                                                    period == null ? null : period.getName(),
                                                    purchase.getBeginDate(), purchase.getEndDate(),
                                                    purchase.getWeeklyDow(),
                                                    Boolean.TRUE.equals(purchase.getDateRangeReq()));
    }

    public List<AccountLedgerResponse> toLedgerResponse(List<AccountLedger> accounts) {
        return new ArrayList<>(accounts.stream().map(AccountMapper::toResponse).toList());
    }

    private static AccountResponse.AccountPaymentResponse toResponse(AccountPayment payment) {
        Item item = payment.getCreditItem();
        Long typeId = item == null || item.getItemType() == null ? null : item.getItemType().getId();
        return new AccountResponse.AccountPaymentResponse(
            payment.getId(), item == null ? null : item.getId(), typeId,
            item == null ? null : item.getName(), item == null ? null : item.getAmount(),
            item == null || item.getTimePeriod() == null ? null : item.getTimePeriod().getName(),
            number(payment.getAmount()), number(payment.getPercent()), perOccurrence(payment));
    }

    private static AccountLedgerResponse toResponse(AccountLedger account) {
        List<Double> balances = new ArrayList<>(account.balances().stream().map(BigDecimal::doubleValue).toList());
        List<AccountLedgerResponse.AccountEventResponse> events = new ArrayList<>(
            account.events().stream().map(AccountMapper::toResponse).toList());

        return new AccountLedgerResponse(account.accountId(), account.name(), kindName(account.kind()),
                                         number(account.apr()), number(account.creditLimit()),
                                         number(account.openingBalance()), account.balanceDate(),
                                         number(account.startingBalance()), number(account.endingBalance()),
                                         number(account.totalPurchases()), number(account.totalPaid()),
                                         number(account.totalInterest()), account.payoffDate(), balances, events);
    }

    private static AccountLedgerResponse.AccountEventResponse toResponse(AccountEvent event) {
        return new AccountLedgerResponse.AccountEventResponse(event.date(), event.kind().name().toLowerCase(),
                                                              event.sourceId(), event.sourceName(),
                                                              number(event.percent()), number(event.amount()),
                                                              number(event.balanceAfter()));
    }

    /** Dollars one occurrence sends: the fixed amount, or the item's amount × percent rounded to cents. */
    private static Double perOccurrence(AccountPayment payment) {
        if (payment.getAmount() != null) {
            return payment.getAmount().doubleValue();
        }
        Item item = payment.getCreditItem();
        Double amount = item == null ? null : item.getAmount();
        if (amount == null || !Double.isFinite(amount) || payment.getPercent() == null) {
            return 0.0;
        }
        return BigDecimal.valueOf(amount).multiply(payment.getPercent())
                         .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)
                         .doubleValue();
    }

    private static BigDecimal decimal(Double value) {
        return value == null || !Double.isFinite(value) ? null : BigDecimal.valueOf(value);
    }

    private static Double number(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }
}
