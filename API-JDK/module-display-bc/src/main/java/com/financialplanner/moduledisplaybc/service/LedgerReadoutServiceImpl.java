package com.financialplanner.moduledisplaybc.service;

import com.financialplanner.moduledisplaybc.model.ItemDto;
import com.financialplanner.moduledisplaybc.model.LedgerDto;
import com.financialplanner.moduledisplaybc.model.LedgerRequest;
import com.financialplanner.moduleitemsbc.domain.service.ItemService;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
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

        // Expand weekly items BEFORE mapping to DTO
        List<ItemDto> itemDtos = expandWeeklyItems(userItems, request.startDate(), request.endDate());

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

    private List<ItemDto> expandWeeklyItems(
        List<Item> items,
        LocalDate ledgerStart,
        LocalDate ledgerEnd
    ) {
        List<ItemDto> expanded = new ArrayList<>();

        for (Item item : items) {

            // Determine periodId safely
            int periodId = Math.toIntExact(item.getTimePeriod() != null ? item.getTimePeriod().getId() : 0);

            // Not weekly → map normally
            if (periodId != 3) {
                expanded.add(mapItemToDto(item));
                continue;
            }

            // Weekly item → weeklyDow MUST exist
            Integer weeklyDow = item.getWeeklyDow();

            if (weeklyDow == null) {
                // Avoid 500 errors — treat as single occurrence
                expanded.add(mapItemToDto(item));
                continue;
            }

            // Convert weeklyDow → DayOfWeek (1=Mon ... 7=Sun)
            DayOfWeek dow = DayOfWeek.of(weeklyDow);

            // Compute weekly dates
            List<LocalDate> weeklyDates = computeWeeklyDates(ledgerStart, ledgerEnd, dow);

            // Clone item for each weekly date
            for (LocalDate date : weeklyDates) {
                ItemDto dto = mapItemToDto(item);
                dto.setOccurrenceDate(date.toString());
                expanded.add(dto);
            }
        }

        return expanded;
    }

    private List<LocalDate> computeWeeklyDates(LocalDate start, LocalDate end, DayOfWeek targetDay) {
        List<LocalDate> dates = new ArrayList<>();

        // Move cursor to the first occurrence of targetDay
        LocalDate cursor = start;
        while (cursor.getDayOfWeek() != targetDay) {
            cursor = cursor.plusDays(1);
        }

        // Collect all weekly occurrences
        while (!cursor.isAfter(end)) {
            dates.add(cursor);
            cursor = cursor.plusWeeks(1);
        }

        return dates;
    }

    private DayOfWeek convertDow(int weeklyDow) {
        return DayOfWeek.of(weeklyDow); // 1 = Monday, 7 = Sunday
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

        dto.setOccurrenceDate(
            i.getBeginDate() != null ? i.getBeginDate().toString() : null
        );

        dto.setPeriod(
            i.getTimePeriod() != null ? i.getTimePeriod().getName() : null
        );

        return dto;
    }

    private List<ItemDto> mapItemsToDto(List<Item> items) {
        return items.stream()
            .map(this::mapItemToDto)
            .collect(Collectors.toList());
    }
}
