package com.financialplanner.moduledisplaybc.recurrence;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import com.financialplanner.moduledisplaybc.model.ItemDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Maps one-time items to ItemDto occurrences.
 * Non one-time items are ignored (not mapped) so other expanders can handle them.
 */
public class OneTimeOccurrenceExpander {

    private final Function<Item, ItemDto> mapper;

    public OneTimeOccurrenceExpander(Function<Item, ItemDto> mapper) {
        this.mapper = mapper;
    }

    /**
     * Return ItemDto occurrences for items that are one-time and whose occurrence
     * falls within the ledger range. Non one-time items are skipped.
     */
    public List<ItemDto> expand(List<Item> items, LocalDate ledgerStart, LocalDate ledgerEnd) {
        List<ItemDto> result = new ArrayList<>();

        for (Item item : items) {

            if (!isOneTime(item)) {
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

    public boolean isOneTime(Item item) {
        if (item == null) return false;

        // Treat missing TimePeriod as one-time (preserves previous behavior)
        if (item.getTimePeriod() == null) return true;

        int pid = Math.toIntExact(item.getTimePeriod().getId());
        // Domain rule: id == 1 => one-time
        return pid == 1;
    }

}
