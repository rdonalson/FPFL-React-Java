package com.financialplanner.moduleapi.dtos.assistant;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

/**
 * A credit or debit the assistant has drafted from what the user described, awaiting the
 * user's review. Nothing here is persisted: {@code ItemDraftTranslator} turns an accepted
 * draft into an {@link com.financialplanner.moduleapi.dtos.item.ItemRequest}, which is what
 * {@code POST /items} already accepts.
 * <p>
 * This is deliberately much narrower than {@code ItemRequest}. That record carries 29 fields
 * with conditional rules; handing it to a language model produces monthly items that also
 * set {@code semiAnnual2Month}. Here every field is either an enum the model must pick from
 * or a single scalar, and which ones are required is a function of {@link #recurrence()}
 * alone.
 * <p>
 * Note that {@code userId} is absent by design. It is resolved from the authenticated
 * principal when the draft is translated, never supplied by the model.
 *
 * @param name             what the user calls this item, e.g. "Rent"
 * @param amount           always positive; {@link #direction()} carries the sign
 * @param direction        money in or money out
 * @param recurrence       how often it occurs; decides which fields below are required
 * @param onDate           required for {@link Recurrence#ONE_TIME}
 * @param onWeekday        required for WEEKLY, EVERY_TWO_WEEKS and NTH_WEEKDAY
 * @param weekOfMonth      required for NTH_WEEKDAY: 1-4, or -1 for the last of the month
 * @param dayOfMonth       required for MONTHLY, and the first day for BI_MONTHLY
 * @param secondDayOfMonth the second day for BI_MONTHLY
 * @param onMonthDays      four pairs for QUARTERLY, two for SEMI_ANNUAL, one for ANNUAL
 * @param beginDate        optional lower bound on when the item applies
 * @param endDate          optional upper bound on when the item applies
 */
public record ItemDraft(

    String name,
    Double amount,
    Direction direction,
    Recurrence recurrence,

    LocalDate onDate,
    DayOfWeek onWeekday,
    Integer weekOfMonth,
    Integer dayOfMonth,
    Integer secondDayOfMonth,
    List<OnMonthDay> onMonthDays,

    LocalDate beginDate,
    LocalDate endDate
) {

    /** Sentinel for {@link #weekOfMonth()} meaning "the last such weekday of the month". */
    public static final int LAST_WEEK_OF_MONTH = -1;

    /**
     * Defensive copy so a draft cannot be mutated after it has been shown to the user.
     */
    public ItemDraft {
        onMonthDays = onMonthDays == null ? List.of() : List.copyOf(onMonthDays);
    }
}
