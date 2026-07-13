package com.financialplanner.moduledisplaybc.utility;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;

import java.time.LocalDate;

public class RecurrenceRange {

    /**
     * Computes the effective date range for an item.
     *
     * If DateRangeReq is true:
     *   - effectiveStart = max(ledgerStart, item.BeginDate)
     *   - effectiveEnd   = min(ledgerEnd,   item.EndDate)
     *
     * If the ranges do not overlap → return null.
     *
     * If DateRangeReq is false → return ledger range.
     */
    public static LocalDate[] resolveRange(Item item, LocalDate ledgerStart, LocalDate ledgerEnd) {

        Boolean req = item.getDateRangeReq();

        // If no override requested → use ledger range
        if (req == null || !req) {
            return new LocalDate[]{ ledgerStart, ledgerEnd };
        }

        LocalDate begin = item.getBeginDate();
        LocalDate end   = item.getEndDate();

        // If item has bad data → fallback to ledger range
        if (begin == null || end == null) {
            return new LocalDate[]{ ledgerStart, ledgerEnd };
        }

        // Compute intersection
        LocalDate effectiveStart = begin.isAfter(ledgerStart) ? begin : ledgerStart;
        LocalDate effectiveEnd   = end.isBefore(ledgerEnd)   ? end   : ledgerEnd;

        // No overlap → skip item entirely
        if (effectiveEnd.isBefore(effectiveStart)) {
            return null;
        }

        return new LocalDate[]{ effectiveStart, effectiveEnd };
    }
}
