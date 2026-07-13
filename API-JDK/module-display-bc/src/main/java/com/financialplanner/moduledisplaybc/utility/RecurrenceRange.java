package com.financialplanner.moduledisplaybc.utility;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;

import java.time.LocalDate;

/**
 * Represents a utility class designed to calculate the effective date range
 * for a given item based on its requested range and a provided ledger range.
 * This class deals with the intersection of date ranges and ensures proper
 * fallback behavior in the case of invalid or missing data.
 */
public class RecurrenceRange {

    /**
     * Resolves the effective date range for the given item by determining the intersection
     * between the item's specified date range and the provided ledger date range. If the item
     * does not request a specific range or has invalid data, the method falls back to using
     * the ledger range. If there is no overlap between the ranges, the method returns null.
     *
     * @param item the item for which the effective range is to be resolved; it may specify
     *             a desired date range through its properties
     * @param ledgerStart the start date of the ledger range
     * @param ledgerEnd the end date of the ledger range
     * @return a two-element array containing the effective start and end dates, or null if
     *         there is no overlap between the calculated and ledger ranges
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
