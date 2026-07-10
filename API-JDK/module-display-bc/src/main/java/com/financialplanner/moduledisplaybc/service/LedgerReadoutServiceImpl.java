package com.financialplanner.moduledisplaybc.service;

import com.financialplanner.moduledisplaybc.model.ItemDto;
import com.financialplanner.moduledisplaybc.model.LedgerDto;
import com.financialplanner.moduledisplaybc.model.LedgerRequest;
import com.financialplanner.moduledisplaybc.recurrence.DailyRecurrenceExpander;
import com.financialplanner.moduledisplaybc.recurrence.OneTimeOccurrenceExpander;
import com.financialplanner.moduledisplaybc.recurrence.WeeklyRecurrenceExpander;
import com.financialplanner.moduleitemsbc.domain.service.ItemService;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service implementation responsible for generating ledger readouts based on user inputs.
 * This class processes and expands user-defined financial items into daily ledger entries
 * that include credits, debits, and running totals.
 * The generated ledger contains aggregated daily financial information enriched with
 * item-specific details, supporting both one-time and recurring (weekly) events.
 * The service is designed to apply an initial running total, daily enrichment based on
 * expanded financial items, and summarizes the daily credit and debit values for each
 * ledger entry.
 * This implementation relies on the following components:
 * - {@link ItemService} for fetching financial items associated with a given user.
 * - {@link WeeklyRecurrenceExpander} for handling weekly recurring events.
 * - {@link OneTimeOccurrenceExpander} for handling one-time financial events.
 */
@Service
public class LedgerReadoutServiceImpl implements LedgerReadoutService {

    private final ItemService itemService;
    private final WeeklyRecurrenceExpander weeklyExpander;
    private final OneTimeOccurrenceExpander oneTimeExpander;
    private final DailyRecurrenceExpander dailyExpander;

    public LedgerReadoutServiceImpl(ItemService itemService) {
        this.itemService = itemService;
        this.weeklyExpander = new WeeklyRecurrenceExpander(this::mapItemToDto);
        this.oneTimeExpander = new OneTimeOccurrenceExpander(this::mapItemToDto);
        this.dailyExpander = new DailyRecurrenceExpander(this::mapItemToDto);
    }

    @Override
    public List<LedgerDto> buildLedgerReadout(LedgerRequest request) {
        List<Item> userItems = itemService.findByUserId(request.userId());

        // 1) One-time occurrences (periodId == 1)
        List<ItemDto> oneTimeDtos = oneTimeExpander.expand(userItems, request.startDate(), request.endDate());

        // 2) Filter out one-time items
        List<Item> nonOneTime = userItems.stream()
            .filter(i -> !oneTimeExpander.isOneTime(i))
            .collect(Collectors.toList());

        // 3) Weekly occurrences (periodId == 3)
        List<ItemDto> weeklyDtos = weeklyExpander.expand(nonOneTime, request.startDate(), request.endDate());

        // 4) Filter out weekly items
        List<Item> nonWeekly = nonOneTime.stream()
            .filter(i -> !weeklyExpander.isWeekly(i))
            .collect(Collectors.toList());

        // 5) Daily occurrences (periodId == 2)
        List<ItemDto> dailyDtos = dailyExpander.expand(nonWeekly, request.startDate(), request.endDate());

        // 6) Combine all DTOs
        List<ItemDto> itemDtos = new ArrayList<>();
        itemDtos.addAll(oneTimeDtos);
        itemDtos.addAll(weeklyDtos);
        itemDtos.addAll(dailyDtos);

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
        return items.stream().filter(i -> i.getFkItemType() == 3).findFirst().map(ItemDto::getAmount).orElse(0.0);
    }

    private void applyDailyEnrichment(List<LedgerDto> ledger, List<ItemDto> items) {

        // Group items by LocalDate
        Map<LocalDate, List<ItemDto>> itemsByDate = items.stream().filter(i -> i.getOccurrenceDate() != null).collect(Collectors.groupingBy(i -> LocalDate.parse(i.getOccurrenceDate())));

        double running = ledger.getFirst().getRunningTotal(); // initial amount

        for (LedgerDto row : ledger) {
            LocalDate date = row.getWDate();
            List<ItemDto> todaysItems = itemsByDate.getOrDefault(date, List.of());

            double credit = 0.0;
            double debit = 0.0;

            int itemKeyCounter = 1;

            for (ItemDto item : todaysItems) {
                double amt = item.getAmount() != null ? item.getAmount() : 0.0;

                switch (item.getFkItemType()) {
                    case 1 -> credit += amt; // credit (positive)
                    case 2 -> debit += amt; // debit stored negative
                    default -> {
                    } // ignore others
                }

                item.setItemKey(itemKeyCounter++);
                row.getItems().add(item);
            }

            double net = credit + debit; // explicit subtraction
            running += net;

            row.setCreditSummary(credit);
            row.setDebitSummary(debit);
            row.setNet(net);
            row.setRunningTotal(running);
        }
    }

    private ItemDto mapItemToDto(Item i) {
        ItemDto dto = new ItemDto();

        // Extract itemType once
        int itemType = Math.toIntExact((i.getItemType() != null ? i.getItemType().getId() : 0));

        // Raw amount (null-safe)
        double rawAmount = (i.getAmount() != null ? i.getAmount() : 0.0);

        // Apply sign based on itemType
        double signedAmount = switch (itemType) {
            case 1 -> rawAmount;        // CREDIT → positive
            case 2 -> -rawAmount;       // DEBIT → negative
            case 3 -> rawAmount;        // INITIAL AMOUNT → positive
            default -> rawAmount;       // fallback
        };

        dto.setItemKey(Math.toIntExact(i.getId()));
        dto.setFkItemType(itemType);
        dto.setItemType(i.getItemType() != null ? i.getItemType().getName() : null);
        dto.setName(i.getName());
        dto.setAmount(signedAmount);

        dto.setOccurrenceDate(i.getBeginDate() != null ? i.getBeginDate().toString() : null);

        dto.setPeriod(i.getTimePeriod() != null ? i.getTimePeriod().getName() : null);

        return dto;
    }
}
