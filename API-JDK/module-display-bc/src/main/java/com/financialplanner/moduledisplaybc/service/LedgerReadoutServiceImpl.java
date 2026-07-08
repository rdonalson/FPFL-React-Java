package com.financialplanner.moduledisplaybc.service;

import com.financialplanner.moduledisplaybc.model.ItemDto;
import com.financialplanner.moduledisplaybc.model.Ledger;
import com.financialplanner.moduledisplaybc.model.LedgerDto;
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
    public List<LedgerDto> buildLedgerReadout(LedgerRequest request) {
        List<Item> userItems = itemService.findByUserId(request.userId());

        // Convert Items → ItemDto
        List<ItemDto> itemDtos = mapItemsToDto(userItems);

        // Build ledger table (DTO)
        List<LedgerDto> ledger = buildLedgerTable(request.startDate(), request.endDate());

        // Initial amount (ItemType 3)
        double initialAmount = extractInitialAmount(itemDtos);

        // Persist initial running total across all days
        applyInitialRunningTotals(ledger, initialAmount);

        // Daily enrichment (add/subtract occurrences)
        applyDailyEnrichment(ledger, itemDtos);

        return ledger;
    }

    private List<LedgerDto> buildLedgerTable(LocalDate start, LocalDate end) {
        List<LedgerDto> table = new ArrayList<>();

        LocalDate cursor = start;
        int rollupKey = 1;

        while (!cursor.isAfter(end)) {
            LedgerDto row = new LedgerDto();
            row.setRollupKey(rollupKey++);
            row.setYear(cursor.getYear());
            row.setWDate(cursor);

            table.add(row);
            cursor = cursor.plusDays(1);
        }

        return table;
    }

    private void applyInitialRunningTotals(List<LedgerDto> ledger, double initialAmount) {
        for (LedgerDto row : ledger) {
            row.setRunningTotal(initialAmount);
        }
    }

    private double extractInitialAmount(List<ItemDto> items) {
        return items.stream()
            .filter(i -> i.getFkItemType() == 3)
            .findFirst()
            .map(ItemDto::getAmount)
            .orElse(0.0);
    }

    private void applyDailyEnrichment(List<LedgerDto> ledger, List<ItemDto> items) {

        // Group items by date
        Map<LocalDate, List<ItemDto>> itemsByDate = items.stream()
            .filter(i -> i.getOccurrenceDate() != null)
            .collect(Collectors.groupingBy(i -> LocalDate.parse(i.getOccurrenceDate())));

        double running = ledger.get(0).getRunningTotal();

        for (LedgerDto row : ledger) {
            LocalDate date = row.getWDate();

            List<ItemDto> todaysItems = itemsByDate.getOrDefault(date, List.of());

            double credit = 0.0;
            double debit = 0.0;

            for (ItemDto item : todaysItems) {
                double amt = item.getAmount() != null ? item.getAmount() : 0.0;

                if (amt > 0.0) credit += amt;
                else debit += amt;

                row.getItems().add(item); // attach items to ledger day
            }

            double net = credit + debit;
            running += net;

            row.setCreditSummary(credit);
            row.setDebitSummary(debit);
            row.setNet(net);
            row.setRunningTotal(running);
        }
    }

    private List<ItemDto> mapItemsToDto(List<Item> items) {
        return items.stream().map(i -> {
            ItemDto dto = new ItemDto();

            dto.setItemKey(Math.toIntExact(i.getId()));
            dto.setFkItemType(Math.toIntExact(i.getItemType().getId()));
            dto.setItemType(i.getItemType().getName());
            dto.setName(i.getName());
            dto.setAmount(i.getAmount());

            // occurrenceDate
            dto.setOccurrenceDate(
                i.getBeginDate() != null ? i.getBeginDate().toString() : null
            );

            // period (NULL SAFE)
            dto.setPeriod(
                i.getTimePeriod() != null ? i.getTimePeriod().getName() : null
            );

            return dto;
        }).collect(Collectors.toList());
    }

}
