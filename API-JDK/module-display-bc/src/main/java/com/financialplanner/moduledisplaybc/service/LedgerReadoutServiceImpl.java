package com.financialplanner.moduledisplaybc.service;

import com.financialplanner.moduledisplaybc.model.ItemDto;
import com.financialplanner.moduledisplaybc.model.LedgerDto;
import com.financialplanner.moduledisplaybc.model.LedgerRequest;
import com.financialplanner.moduledisplaybc.recurrence.BiWeeklyRecurrenceExpander;
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
 * Implementation of the {@link LedgerReadoutService} interface. This service is responsible for
 * constructing a detailed ledger readout for a specific user over a given time period. It processes
 * recurring and one-time items, grouping and aggregating them into a comprehensive list of ledger
 * entries that includes credits, debits, net amounts, and running totals for each day.
 * This implementation uses various recurrence expanders to handle different types of items:
 * - Weekly recurring items
 * - Bi-weekly recurring items
 * - Daily recurring items
 * - One-time occurrences
 * The service performs the following high-level tasks:
 * - Fetches user-specific items from the underlying data source
 * - Maps items to DTO representations
 * - Expands recurring items based on their recurrence type and date range
 * - Constructs a ledger table for the given time range
 * - Applies running totals, credit, debit, and net calculations to the ledger
 * - Groups and applies daily transaction enrichment for accurate representation
 * The implementation is stateless and relies on dependency injection for managing its components.
 */
@Service
public class LedgerReadoutServiceImpl implements LedgerReadoutService {

    private final ItemService itemService;
    private final WeeklyRecurrenceExpander weeklyExpander;
    private final OneTimeOccurrenceExpander oneTimeExpander;
    private final DailyRecurrenceExpander dailyExpander;
    private final BiWeeklyRecurrenceExpander biWeeklyExpander;

    public LedgerReadoutServiceImpl(ItemService itemService) {
        this.itemService = itemService;
        this.weeklyExpander = new WeeklyRecurrenceExpander(this::mapItemToDto);
        this.oneTimeExpander = new OneTimeOccurrenceExpander(this::mapItemToDto);
        this.dailyExpander = new DailyRecurrenceExpander(this::mapItemToDto);
        this.biWeeklyExpander = new BiWeeklyRecurrenceExpander(this::mapItemToDto);
    }

    /**
     * Builds a ledger readout for a specific user and time period, aggregating and enriching recurring
     * and one-time items into a structured list of ledger entries.
     *
     * @param request the ledger request containing the user ID and date range for the ledger readout
     * @return a list of ledger entries (LedgerDto) representing the user's financial activity
     *         within the specified date range
     */
    @Override
    public List<LedgerDto> buildLedgerReadout(LedgerRequest request) {
        List<Item> userItems = itemService.findByUserId(request.userId());

        LocalDate start = request.startDate();
        LocalDate end = request.endDate();

        // Each expander evaluates ALL items and expands only its own period.
        // Non-matching items are ignored inside the expander.

        List<ItemDto> oneTimeDtos   = oneTimeExpander.expand(userItems, start, end);
        List<ItemDto> weeklyDtos    = weeklyExpander.expand(userItems, start, end);
        List<ItemDto> biWeeklyDtos  = biWeeklyExpander.expand(userItems, start, end);
        List<ItemDto> dailyDtos     = dailyExpander.expand(userItems, start, end);

        // Combine all occurrences from all recurrence types
        List<ItemDto> itemDtos = new ArrayList<>();
        itemDtos.addAll(oneTimeDtos);
        itemDtos.addAll(weeklyDtos);
        itemDtos.addAll(biWeeklyDtos);
        itemDtos.addAll(dailyDtos);

        // Build ledger table (DTO)
        List<LedgerDto> ledger = buildLedgerTable(start, end);

        // Initial amount (ItemType 3)
        double initialAmount = extractInitialAmount(itemDtos);

        // Persist initial running total across all days
        applyInitialRunningTotals(ledger, initialAmount);

        // Daily enrichment (add/subtract occurrences)
        applyDailyEnrichment(ledger, itemDtos);

        return ledger;
    }


    /**
     * Constructs a ledger table containing a list of {@code LedgerDto} objects for each date
     * within the provided start and end date range, inclusive. Each ledger row includes the rollup key,
     * the year, and the specific date.
     *
     * @param start the starting date of the ledger table (inclusive)
     * @param end the ending date of the ledger table (inclusive)
     * @return a list of {@code LedgerDto} objects representing the ledger table for the given date range
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
     * Updates each entry in the provided ledger with an initial running total amount.
     *
     * @param ledger the list of ledger entries that will have their running totals updated
     * @param initialAmount the initial amount to set as the running total for each ledger entry
     */
    private void applyInitialRunningTotals(List<LedgerDto> ledger, double initialAmount) {
        for (LedgerDto row : ledger) {
            row.setRunningTotal(initialAmount);
        }
    }

    /**
     * Extracts the initial amount from a list of items by filtering items of type 3
     * and retrieving the amount of the first matching item. If no matching item is
     * found, returns a default value of 0.0.
     *
     * @param items the list of {@code ItemDto} objects to search for the initial amount
     * @return the initial amount as a double; 0.0 if no matching item is found
     */
    private double extractInitialAmount(List<ItemDto> items) {
        return items.stream().filter(i -> i.getFkItemType() == 3).findFirst().map(ItemDto::getAmount).orElse(0.0);
    }

    /**
     * Applies daily enrichment to a list of ledger entries by processing a list of items
     * and updating the ledger with credit, debit, and running total calculations.
     * Items are grouped by their occurrence date and mapped to corresponding ledger entries.
     *
     * @param ledger the list of ledger entries to be updated. Each entry represents a daily record.
     * @param items the list of items containing transaction details that are to be grouped by their occurrence date and applied to the ledger.
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
     * Maps an {@link Item} entity to an {@link ItemDto} instance.
     * Converts and sets properties from an {@link Item} to an {@link ItemDto},
     * handling null-safety and applying business logic such as amount sign determination
     * based on the item type.
     *
     * @param i the {@link Item} entity to be mapped. Must not be null.
     * @return an {@link ItemDto} representation of the provided {@link Item}.
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
