package com.financialplanner.moduledisplaybc.service;

import com.financialplanner.moduledisplaybc.model.ItemDto;
import com.financialplanner.moduledisplaybc.model.LedgerDto;
import com.financialplanner.moduledisplaybc.model.LedgerRequest;
import com.financialplanner.moduledisplaybc.recurrence.DailyRecurrenceExpander;
import com.financialplanner.moduledisplaybc.recurrence.OneTimeOccurrenceExpander;
import com.financialplanner.moduledisplaybc.recurrence.WeeklyRecurrenceExpander;
import com.financialplanner.moduleitemsbc.domain.service.ItemService;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link LedgerReadoutService} that generates a financial ledger
 * readout for a specified user and date range. This service handles the expansion of
 * recurring items and one-time occurrences, processes financial transactions, and
 * enriches the ledger entries with detailed calculations.
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

    /**
     * Builds a ledger readout for a specified user and date range.
     * This method processes user items, filters them based on occurrence periods
     * (one-time, weekly, and daily), converts them to DTOs, and generates a ledger
     * consisting of enriched daily transactions with a running total.
     *
     * @param request the {@code LedgerRequest} object containing the user ID,
     *                start date, and end date for which the ledger is to be generated.
     * @return a {@code List<LedgerDto>} containing the ledger entries, enriched with
     *         daily transactions and a running total for the specified date range.
     */
    @Override
    public List<LedgerDto> buildLedgerReadout(LedgerRequest request) {
        List<Item> userItems = itemService.findByUserId(request.userId());

        // 1) One-time occurrences (periodId == 1)
        List<ItemDto> oneTimeDtos = oneTimeExpander.expand(userItems, request.startDate(), request.endDate());

        // 2) Filter out one-time items
        List<Item> nonOneTime = userItems.stream()
            .filter(oneTimeExpander::isOneTime)
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

    /**
     * Builds a ledger table containing a list of ledger entries for each day in the given date range.
     *
     * @param start the start date for the ledger table
     * @param end the end date for the ledger table
     * @return a list of {@code LedgerDto} objects representing the ledger rows for each day in the date range
     */
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

    /**
     * Updates the running total for each entry in the provided ledger by applying the initial amount.
     * The running total for all rows will be set to the specified initial amount.
     *
     * @param ledger the list of {@code LedgerDto} objects representing the ledger entries to be updated
     * @param initialAmount the initial amount to be applied as the running total for each ledger entry
     */
    private void applyInitialRunningTotals(List<LedgerDto> ledger, double initialAmount) {
        for (LedgerDto row : ledger) {
            row.setRunningTotal(initialAmount);
        }
    }

    /**
     * Extracts the initial amount from a list of financial items by identifying the first
     * item with an item type matching the identifier for "Initial Amount" (itemType = 3).
     * If no such item is found, the method returns 0.0.
     *
     * @param items the list of {@code ItemDto} objects representing financial items to scan.
     * @return the amount of the first item with item type 3, or 0.0 if no such item exists.
     */
    private double extractInitialAmount(List<ItemDto> items) {
        return items.stream().filter(i -> i.getFkItemType() == 3).findFirst().map(ItemDto::getAmount).orElse(0.0);
    }

    /**
     * Applies daily enrichment to the given ledger by processing a list of items and updating
     * the ledger with calculated credit, debit, net, and running totals for each date, along
     * with adding the items to the corresponding ledger entries.
     *
     * @param ledger the list of LedgerDto representing the ledger data to be enriched.
     *               Each entry in the ledger corresponds to a specific date.
     * @param items the list of ItemDto representing the items to be processed and
     *              grouped by their occurrence date for enrichment of the ledger.
     */
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

    /**
     * Converts an {@link Item} entity to an {@link ItemDto}.
     * Maps relevant fields from the source {@code Item} object to the target {@code ItemDto}.
     * This includes processing and transforming data such as the amount, item type, and related dates.
     *
     * @param i The {@code Item} object to be mapped. Must not be null.
     * @return The mapped {@code ItemDto} containing the transformed data from the input {@code Item}.
     */
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
