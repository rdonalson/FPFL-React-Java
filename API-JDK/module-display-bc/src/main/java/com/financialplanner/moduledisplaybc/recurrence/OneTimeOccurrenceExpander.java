package com.financialplanner.moduledisplaybc.recurrence;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import com.financialplanner.moduledisplaybc.model.ItemDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * A utility class responsible for expanding and mapping one-time occurrences
 * within a provided range of ledger dates. This class processes a list of
 * domain-specific items, filters for one-time occurrences, maps them to
 * presentation-friendly DTOs, and returns the results. Non one-time items
 * are ignored and delegated to external processors.
 */
public class OneTimeOccurrenceExpander {

    private final Function<Item, ItemDto> mapper;

    public OneTimeOccurrenceExpander(Function<Item, ItemDto> mapper) {
        this.mapper = mapper;
    }

    /**
     * Expands a list of items by extracting and mapping one-time occurrences
     * within the specified ledger date range. Non one-time items are ignored,
     * and their handling is delegated to other expanders.
     *
     * @param items the list of items to be expanded and processed
     * @param ledgerStart the start of the ledger date range
     * @param ledgerEnd the end of the ledger date range
     * @return a list of {@code ItemDto} objects representing one-time occurrences
     *         mapped within the specified ledger date range
     */
    public List<ItemDto> expand(List<Item> items, LocalDate ledgerStart, LocalDate ledgerEnd) {
        List<ItemDto> result = new ArrayList<>();

        for (Item item : items) {

            if (isOneTime(item)) {
                // skip non one-time items; other expanders will handle them
                continue;
            }

            // map to DTO
            ItemDto dto = mapper.apply(item);

            // ensure occurrenceDate comes from beginDate and is within range
            if (item.getBeginDate() != null) {
                LocalDate occ = item.getBeginDate();
                if (!occ.isBefore(ledgerStart) && !occ.isAfter(ledgerEnd)) {
                    dto.setOccurrenceDate(occ.toString());
                    result.add(dto);
                }
                // else: one-time occurrence outside ledger range -> ignore
            }
        }

        return result;
    }

    /**
     * Determines whether the specified {@code Item} is considered a one-time occurrence.
     * An item is treated as one-time based on the associated {@code TimePeriod}'s ID.
     * If the item is {@code null}, it is considered one-time by default.
     * If the {@code TimePeriod} of the item is {@code null}, it is not considered one-time.
     * When the ID of the {@code TimePeriod} is equal to 1, the item is classified as one-time.
     * Otherwise, it is not considered one-time.
     *
     * @param item the {@code Item} to evaluate; can be {@code null}
     * @return {@code true} if the given {@code Item} is a one-time occurrence; {@code false} otherwise
     */
    public boolean isOneTime(Item item) {
        if (item == null) return true;

        // Treat missing TimePeriod as one-time (preserves previous behavior)
        if (item.getTimePeriod() == null) return false;

        int pid = Math.toIntExact(item.getTimePeriod().getId());
        // Domain rule: id == 1 => one-time
        return pid != 1;
    }

}
