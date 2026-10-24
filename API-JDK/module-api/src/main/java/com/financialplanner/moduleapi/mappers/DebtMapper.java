package com.financialplanner.moduleapi.mappers;

import com.financialplanner.moduleapi.dtos.debt.DebtRequest;
import com.financialplanner.moduleapi.dtos.debt.DebtResponse;
import com.financialplanner.moduleapi.dtos.paydown.DebtPaydownResponse;
import com.financialplanner.moduledisplaybc.paydown.PaydownModels.DebtPaydown;
import com.financialplanner.moduledisplaybc.paydown.PaydownModels.PaydownEvent;
import com.financialplanner.moduleitemsbc.domain.debt.DebtDraft;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Debt;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.DebtAllocation;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Debt Paydown mapping between requests, entities, calculator results and responses. Money leaves the API
 * as plain numbers (like the ledger's amounts); outgoing data is sanitized by the response factory.
 */
@Component
public class DebtMapper {

    public DebtDraft toDraft(DebtRequest request) {
        List<DebtDraft.AllocationDraft> allocations = new ArrayList<>();
        if (request.allocations() != null) {
            for (DebtRequest.DebtAllocationRequest a : request.allocations()) {
                allocations.add(a == null ? new DebtDraft.AllocationDraft(null, null)
                                          : new DebtDraft.AllocationDraft(a.creditItemId(), decimal(a.percent())));
            }
        }
        return new DebtDraft(request.itemId(), decimal(request.balance()), decimal(request.apr()),
                             request.balanceDate(), allocations);
    }

    public DebtResponse toResponse(Debt debt) {
        Item item = debt.getItem();
        List<DebtResponse.DebtAllocationResponse> allocations = new ArrayList<>(
            debt.getAllocations().stream()
                .sorted(Comparator.comparing((DebtAllocation a) -> a.getCreditItem().getName(),
                                             Comparator.nullsLast(Comparator.naturalOrder())))
                .map(DebtMapper::toResponse)
                .toList());

        return new DebtResponse(debt.getId(), item.getId(), item.getName(), item.getAmount(), periodName(item),
                                number(debt.getBalance()), number(debt.getApr()), debt.getBalanceDate(), allocations);
    }

    public List<DebtResponse> toResponse(List<Debt> debts) {
        return new ArrayList<>(debts.stream().map(this::toResponse).toList());
    }

    public List<DebtPaydownResponse> toPaydownResponse(List<DebtPaydown> debts) {
        return new ArrayList<>(debts.stream().map(DebtMapper::toResponse).toList());
    }

    private static DebtResponse.DebtAllocationResponse toResponse(DebtAllocation allocation) {
        Item credit = allocation.getCreditItem();
        return new DebtResponse.DebtAllocationResponse(allocation.getId(), credit.getId(), credit.getName(),
                                                       credit.getAmount(), periodName(credit),
                                                       number(allocation.getPercent()),
                                                       perOccurrence(credit.getAmount(), allocation.getPercent()));
    }

    private static DebtPaydownResponse toResponse(DebtPaydown debt) {
        List<Double> balances = new ArrayList<>(debt.balances().stream().map(BigDecimal::doubleValue).toList());
        List<DebtPaydownResponse.PaydownEventResponse> events = new ArrayList<>(
            debt.events().stream().map(DebtMapper::toResponse).toList());

        return new DebtPaydownResponse(debt.debtId(), debt.debtItemId(), debt.name(), number(debt.apr()),
                                       number(debt.enteredBalance()), debt.balanceDate(),
                                       number(debt.startingBalance()), number(debt.endingBalance()),
                                       number(debt.totalPaid()), number(debt.totalInterest()), debt.payoffDate(),
                                       balances, events);
    }

    private static DebtPaydownResponse.PaydownEventResponse toResponse(PaydownEvent event) {
        return new DebtPaydownResponse.PaydownEventResponse(event.date(), event.kind().name().toLowerCase(),
                                                            event.sourceItemId(), event.sourceName(),
                                                            number(event.percent()), number(event.amount()),
                                                            number(event.balanceAfter()));
    }

    private static String periodName(Item item) {
        return item.getTimePeriod() != null ? item.getTimePeriod().getName() : null;
    }

    /** Dollars one occurrence of the credit sends, rounded to cents as the calculator does. */
    private static Double perOccurrence(Double amount, BigDecimal percent) {
        if (amount == null || !Double.isFinite(amount) || percent == null) {
            return 0.0;
        }
        return BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP)
                         .multiply(percent)
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
