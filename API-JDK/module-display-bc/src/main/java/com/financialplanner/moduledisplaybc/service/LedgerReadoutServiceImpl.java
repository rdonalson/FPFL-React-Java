package com.financialplanner.moduledisplaybc.service;

import com.financialplanner.moduledisplaybc.model.Ledger;
import com.financialplanner.moduledisplaybc.model.LedgerRequest;
import com.financialplanner.moduleitemsbc.domain.service.ItemService;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LedgerReadoutServiceImpl implements LedgerReadoutService {

    private final ItemService itemService;

    public LedgerReadoutServiceImpl(ItemService itemService) {
        this.itemService = itemService;
    }

    @Override
    public List<Ledger> buildLedgerReadout(LedgerRequest request) {
        List<Item> userItems = itemService.findByUserId(request.userId());
        List<Ledger> ledger = buildLedgerTable(request.startDate(), request.endDate());

        // 1. Initial amount (ItemType 3)
        double initialAmount = extractInitialAmount(userItems);

        // 2. Persist initial running total across all days
        applyInitialRunningTotals(ledger, initialAmount);

        // 3. Daily enrichment (add/subtract occurrences)
        applyDailyEnrichment(ledger, userItems);

        return ledger;
    }

    private List<Ledger> buildLedgerTable(LocalDate start, LocalDate end) {
        List<Ledger> table = new ArrayList<>();

        LocalDate cursor = start;
        int rollupKey = 1;

        while (!cursor.isAfter(end)) {
            Ledger row = new Ledger();
            row.setRollupKey(rollupKey++);
            row.setYear(cursor.getYear());
            row.setWDate(cursor);

            table.add(row);
            cursor = cursor.plusDays(1);
        }

        return table;
    }

    private void applyInitialRunningTotals(List<Ledger> ledger, double initialAmount) {
        double running;
        running = initialAmount;

        for (Ledger row : ledger) {
            row.setRunningTotal(running);
        }
    }

    private double extractInitialAmount(List<Item> userItems) {
        return userItems.stream()
            .filter(i -> i.getItemType().getId() == 3)   // ItemType 3 = Initial Amount
            .findFirst()
            .map(Item::getAmount)
            .orElse(0.0);
    }

    private void applyDailyEnrichment(List<Ledger> ledger, List<Item> userItems) {
        // Filter out items with null dates (or change to sentinel grouping if you prefer)
        Map<LocalDate, List<Item>> itemsByDate = userItems.stream()
            .filter(i -> i.getBeginDate() != null)
            .collect(Collectors.groupingBy(Item::getBeginDate));

        double running = ledger.isEmpty() ? 0.0 : ledger.get(0).getRunningTotal();

        for (Ledger row : ledger) {
            LocalDate date = row.getWDate();
            List<Item> todaysItems = itemsByDate.getOrDefault(date, List.of());

            double credit = 0.0;
            double debit = 0.0;

            for (Item item : todaysItems) {
                // Defensive: ensure amount is not null (if amount is Double object)
                double amt = item.getAmount(); // adjust if Item.amount is Double
                if (amt > 0.0) credit += amt;
                else debit += amt;
            }

            double net = credit + debit;
            running += net;

            row.setCreditSummary(credit);
            row.setDebitSummary(debit);
            row.setNet(net);
            row.setRunningTotal(running);
        }
    }
}
