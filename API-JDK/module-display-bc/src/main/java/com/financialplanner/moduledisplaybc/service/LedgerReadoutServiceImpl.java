package com.financialplanner.moduledisplaybc.service;

import com.financialplanner.moduledisplaybc.model.ItemDto;
import com.financialplanner.moduledisplaybc.model.LedgerDto;
import com.financialplanner.moduledisplaybc.model.LedgerRequest;
import com.financialplanner.moduledisplaybc.recurrence.*;
import com.financialplanner.moduleitemsbc.domain.service.ItemService;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service implementation responsible for constructing ledger readouts based on user-specific
 * items and their recurrence types within a specified date range. The service integrates data
 * transformations and computations across multiple recurrence types while generating a detailed
 * daily ledger summary.
 */
@Service
public class LedgerReadoutServiceImpl implements LedgerReadoutService {

    private final ItemService itemService;
    private final OneTimeOccurrenceExpander oneTimeExpander;
    private final DailyRecurrenceExpander dailyExpander;
    private final WeeklyRecurrenceExpander weeklyExpander;
    private final BiWeeklyRecurrenceExpander biWeeklyExpander;
    private final BiMonthlyRecurrenceExpander biMonthlyExpander;
    private final MonthlyRecurrenceExpander monthlyExpander;
    private final QuarterlyRecurrenceExpander quarterlyExpander;
    private final SemiAnnualRecurrenceExpander semiAnnualExpander;
    private final AnnualRecurrenceExpander annualExpander;

    public LedgerReadoutServiceImpl(ItemService itemService) {
        this.itemService = itemService;
        this.oneTimeExpander = new OneTimeOccurrenceExpander(this::mapItemToDto);
        this.dailyExpander = new DailyRecurrenceExpander(this::mapItemToDto);
        this.weeklyExpander = new WeeklyRecurrenceExpander(this::mapItemToDto);
        this.biWeeklyExpander = new BiWeeklyRecurrenceExpander(this::mapItemToDto);
        this.biMonthlyExpander = new BiMonthlyRecurrenceExpander(this::mapItemToDto);
        this.monthlyExpander = new MonthlyRecurrenceExpander(this::mapItemToDto);
        this.quarterlyExpander = new QuarterlyRecurrenceExpander(this::mapItemToDto);
        this.semiAnnualExpander = new SemiAnnualRecurrenceExpander(this::mapItemToDto);
        this.annualExpander = new AnnualRecurrenceExpander(this::mapItemToDto);
    }

    /**
     * Builds a ledger readout based on the provided {@link LedgerRequest}, detailing financial occurrences
     * and calculating running totals over a specified date range.
     * The method retrieves a list of items associated with the user ID from the request,
     * processes those items according to their recurrence types, and combines them into a unified daily ledger. It
     * applies calculations for an initial amount and daily adjustments.
     * @param request the {@link LedgerRequest} containing the user ID, start date, and end date
     *                for which to build the ledger readout
     * @return a list of {@link LedgerDto} objects representing the detailed ledger with daily finances
     */
    @Override
    public List<LedgerDto> buildLedgerReadout(LedgerRequest request) {
        List<Item> userItems = itemService.findByUserId(request.userId());

        LocalDate start = request.startDate();
        LocalDate end = request.endDate();

        // Each expander evaluates ALL items and expands only its own period.
        // Non-matching items are ignored inside the expander.

        List<ItemDto> oneTimeDtos   = oneTimeExpander.expand(userItems, start, end);
        List<ItemDto> dailyDtos     = dailyExpander.expand(userItems, start, end);
        List<ItemDto> weeklyDtos    = weeklyExpander.expand(userItems, start, end);
        List<ItemDto> biWeeklyDtos  = biWeeklyExpander.expand(userItems, start, end);
        List<ItemDto> biMonthlyDtos = biMonthlyExpander.expand(userItems, start, end);
        List<ItemDto> monthlyDtos   = monthlyExpander.expand(userItems, start, end);
        List<ItemDto> quarterlyDtos = quarterlyExpander.expand(userItems, start, end);
        List<ItemDto> semiAnnualDtos = semiAnnualExpander.expand(userItems, start, end);
        List<ItemDto> annualDtos = annualExpander.expand(userItems, start, end);

        // Combine all occurrences from all recurrence types
        List<ItemDto> itemDtos = new ArrayList<>();
        itemDtos.addAll(oneTimeDtos);
        itemDtos.addAll(dailyDtos);
        itemDtos.addAll(weeklyDtos);
        itemDtos.addAll(biWeeklyDtos);
        itemDtos.addAll(biMonthlyDtos);
        itemDtos.addAll(monthlyDtos);
        itemDtos.addAll(quarterlyDtos);
        itemDtos.addAll(semiAnnualDtos);
        itemDtos.addAll(annualDtos);

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
     * Constructs a ledger table for the given date range. Each entry in the table corresponds to a
     * specific date within the range, and includes details such as the rollup key, year, and date.
     *
     * @param start the starting date of the ledger table (inclusive)
     * @param end the ending date of the ledger table (inclusive)
     * @return a list of {@code LedgerDto} objects representing the ledger table for the specified date range
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
     * Applies the initial running total to each entry in the provided ledger.
     *
     * @param ledger the list of ledger entries to which the running total will be applied
     * @param initialAmount the initial amount to set as the running total for each ledger entry
     */
    private void applyInitialRunningTotals(List<LedgerDto> ledger, double initialAmount) {
        for (LedgerDto row : ledger) {
            row.setRunningTotal(initialAmount);
        }
    }

    /**
     * Extracts the initial amount from a list of items by finding the first item with a specific item type.
     *
     * @param items the list of items to process
     * @return the amount of the first item with an item type of 3, or 0.0 if no such item exists
     */
    private double extractInitialAmount(List<ItemDto> items) {
        return items.stream().filter(i -> i.getFkItemType() == 3).findFirst().map(ItemDto::getAmount).orElse(0.0);
    }

    /**
     * Applies daily enrichment to a ledger by processing associated items for each date
     * and updating the ledger with credit, debit, net, and running total values.
     *
     * @param ledger A list of {@link LedgerDto} objects representing the ledger entries.
     *               Each entry contains details for a specific date.
     * @param items A list of {@link ItemDto} objects representing items to be processed.
     *              Each item is associated with a specific occurrence date and type (credit or debit).
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
     * Maps an {@code Item} object to an {@code ItemDto} object.
     *
     * @param i the {@code Item} to be mapped, which contains the data to transform.
     *          If any field in the {@code Item} is null, a fallback or default value
     *          will be applied during the mapping process.
     * @return an {@code ItemDto} object containing the mapped data such as the item key,
     *         type, name, signed amount, occurrence date, and period.
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
