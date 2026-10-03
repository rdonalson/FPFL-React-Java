package com.financialplanner.moduleapi.mappers;

import com.financialplanner.moduleapi.dtos.assistant.InitialAmountDraft;
import com.financialplanner.moduleapi.dtos.assistant.ItemDraft;
import com.financialplanner.moduleapi.dtos.assistant.OnMonthDay;
import com.financialplanner.moduleapi.dtos.initialamount.InitialAmountRequest;
import com.financialplanner.moduleapi.dtos.item.ItemRequest;
import com.financialplanner.modulecommonbc.exception.DomainValidationException;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Turns an {@link ItemDraft} produced by the assistant into the {@link ItemRequest} that
 * {@code POST /items} already accepts.
 * <p>
 * This class is the boundary between a shape a language model can reliably produce and a
 * shape the domain requires. It holds three responsibilities and no others:
 * <ol>
 *   <li>Reject drafts that are incomplete or internally inconsistent, with messages written
 *       to be read by the model so it can correct itself and retry.</li>
 *   <li>Expand the draft's single recurrence choice into the specific day fields that
 *       recurrence uses, leaving every other field null.</li>
 *   <li>Attach the authenticated user's id, which never travels through the model.</li>
 * </ol>
 * The compact constructor on {@code ItemRequest} remains the final backstop: anything that
 * slips past this class is still rejected there.
 */
@Component
public class ItemDraftTranslator {

    /** Valid values for {@code weekOfMonth}: the first four, or -1 meaning the last. */
    private static final Set<Integer> VALID_WEEKS_OF_MONTH =
        Set.of(1, 2, 3, 4, ItemDraft.LAST_WEEK_OF_MONTH);

    private static final int MIN_MONTH = 1;
    private static final int MAX_MONTH = 12;
    private static final int MIN_DAY   = 1;
    private static final int MAX_DAY   = 31;

    /** Month/day pairs are ordered so quarterly slot 1 is the earliest in the year. */
    private static final Comparator<OnMonthDay> BY_MONTH_THEN_DAY =
        Comparator.comparingInt(OnMonthDay::month).thenComparingInt(OnMonthDay::day);

    /**
     * Validates a draft and returns it unchanged. Called by the assistant's
     * {@code proposeItem} tool so the model learns about a bad draft immediately, before
     * anything is shown to the user.
     *
     * @param draft the draft to check
     * @return the same draft, if it is valid
     * @throws DomainValidationException if the draft is incomplete or inconsistent
     */
    public ItemDraft validate(ItemDraft draft) {
        if (draft == null) {
            throw new DomainValidationException("No draft was supplied");
        }
        if (draft.name() == null || draft.name().isBlank()) {
            throw new DomainValidationException("The item needs a name. Ask the user what to call it.");
        }
        if (draft.amount() == null || draft.amount() <= 0) {
            throw new DomainValidationException(
                "The amount must be a positive number. Direction carries the sign, not the amount.");
        }
        if (draft.direction() == null) {
            throw new DomainValidationException("Set direction to CREDIT for money in, or DEBIT for money out.");
        }
        if (draft.recurrence() == null) {
            throw new DomainValidationException(
                "Set a recurrence. Ask the user how often this happens rather than assuming.");
        }
        if (draft.beginDate() != null && draft.endDate() != null
            && draft.beginDate().isAfter(draft.endDate())) {
            throw new DomainValidationException("beginDate must be on or before endDate.");
        }

        switch (draft.recurrence()) {
            case ONE_TIME -> requireDate(draft.onDate(), "onDate", "a one-time item");
            case DAILY -> { /* a daily item needs nothing beyond the range */ }
            case WEEKLY, EVERY_TWO_WEEKS -> requireWeekday(draft.onWeekday(), draft.recurrence().name());
            case NTH_WEEKDAY -> {
                requireWeekday(draft.onWeekday(), "NTH_WEEKDAY");
                if (draft.weekOfMonth() == null || !VALID_WEEKS_OF_MONTH.contains(draft.weekOfMonth())) {
                    throw new DomainValidationException(
                        "weekOfMonth must be 1, 2, 3, 4, or -1 for the last weekday of the month.");
                }
            }
            case MONTHLY -> requireDayOfMonth(draft.dayOfMonth(), "dayOfMonth", "a monthly item");
            case BI_MONTHLY -> {
                requireDayOfMonth(draft.dayOfMonth(), "dayOfMonth", "a bi-monthly item");
                requireDayOfMonth(draft.secondDayOfMonth(), "secondDayOfMonth", "a bi-monthly item");
                if (draft.dayOfMonth().equals(draft.secondDayOfMonth())) {
                    throw new DomainValidationException(
                        "A bi-monthly item needs two different days of the month.");
                }
            }
            case QUARTERLY   -> requireMonthDays(draft.onMonthDays(), 4, "QUARTERLY");
            case SEMI_ANNUAL -> requireMonthDays(draft.onMonthDays(), 2, "SEMI_ANNUAL");
            case ANNUAL      -> requireMonthDays(draft.onMonthDays(), 1, "ANNUAL");
        }

        return draft;
    }

    /**
     * Translates a validated draft into an {@link ItemRequest} for the given user.
     *
     * @param draft  the draft the user accepted
     * @param userId resolved from the authenticated principal, never from the model
     * @return a request ready for {@code ItemService}
     * @throws DomainValidationException if the draft is invalid or the user id is missing
     */
    public ItemRequest toItemRequest(ItemDraft draft, UUID userId) {
        validate(draft);
        if (userId == null) {
            throw new DomainValidationException("No authenticated user; refusing to build an item request");
        }

        Integer weeklyDow = null;
        Integer everyOtherWeekDow = null;
        Integer biMonthlyDay1 = null;
        Integer biMonthlyDay2 = null;
        Integer monthlyDom = null;
        Integer q1Month = null, q1Day = null, q2Month = null, q2Day = null;
        Integer q3Month = null, q3Day = null, q4Month = null, q4Day = null;
        Integer semi1Month = null, semi1Day = null, semi2Month = null, semi2Day = null;
        Integer annualMoy = null, annualDom = null;
        Integer nthDow = null, nthIndex = null;

        LocalDate beginDate = draft.beginDate();
        LocalDate endDate   = draft.endDate();
        boolean dateRangeReq = beginDate != null || endDate != null;

        List<OnMonthDay> monthDays = draft.onMonthDays()
                                          .stream()
                                          .sorted(BY_MONTH_THEN_DAY)
                                          .toList();

        switch (draft.recurrence()) {
            case ONE_TIME -> {
                // Mirrors OneTimeForm: a single occurrence is stored as a bounded range
                // carrying only a begin date.
                beginDate    = draft.onDate();
                endDate      = null;
                dateRangeReq = true;
            }
            case DAILY -> { /* no day fields */ }
            case WEEKLY          -> weeklyDow         = isoDayOfWeek(draft.onWeekday());
            case EVERY_TWO_WEEKS -> everyOtherWeekDow = isoDayOfWeek(draft.onWeekday());
            case NTH_WEEKDAY -> {
                nthDow   = isoDayOfWeek(draft.onWeekday());
                nthIndex = draft.weekOfMonth();
            }
            case MONTHLY -> monthlyDom = draft.dayOfMonth();
            case BI_MONTHLY -> {
                biMonthlyDay1 = draft.dayOfMonth();
                biMonthlyDay2 = draft.secondDayOfMonth();
            }
            case QUARTERLY -> {
                q1Month = monthDays.get(0).month(); q1Day = monthDays.get(0).day();
                q2Month = monthDays.get(1).month(); q2Day = monthDays.get(1).day();
                q3Month = monthDays.get(2).month(); q3Day = monthDays.get(2).day();
                q4Month = monthDays.get(3).month(); q4Day = monthDays.get(3).day();
            }
            case SEMI_ANNUAL -> {
                semi1Month = monthDays.get(0).month(); semi1Day = monthDays.get(0).day();
                semi2Month = monthDays.get(1).month(); semi2Day = monthDays.get(1).day();
            }
            case ANNUAL -> {
                annualMoy = monthDays.get(0).month();
                annualDom = monthDays.get(0).day();
            }
        }

        return new ItemRequest(
            userId,
            draft.name().trim(),
            draft.amount(),
            draft.direction().fkItemType(),
            draft.recurrence().fkPeriod(),
            beginDate,
            endDate,
            weeklyDow,
            everyOtherWeekDow,
            biMonthlyDay1,
            biMonthlyDay2,
            monthlyDom,
            q1Month, q1Day,
            q2Month, q2Day,
            q3Month, q3Day,
            q4Month, q4Day,
            semi1Month, semi1Day,
            semi2Month, semi2Day,
            annualMoy,
            annualDom,
            nthDow,
            nthIndex,
            dateRangeReq
        );
    }

    /**
     * Translates a starting-balance draft. Unlike an item, this amount may be negative.
     *
     * @param draft  the drafted starting balance
     * @param userId resolved from the authenticated principal
     * @return a request ready for {@code InitialAmountController}
     */
    public InitialAmountRequest toInitialAmountRequest(InitialAmountDraft draft, UUID userId) {
        if (draft == null || draft.amount() == null) {
            throw new DomainValidationException("Ask the user for their starting balance before proposing one.");
        }
        if (userId == null) {
            throw new DomainValidationException("No authenticated user; refusing to build an initial amount request");
        }
        return new InitialAmountRequest(userId, draft.amount());
    }

    /**
     * Converts a {@link DayOfWeek} to the ISO number the recurrence expanders expect.
     * <p>
     * The expanders in {@code module-display-bc} call {@code DayOfWeek.of(value)}, which is
     * 1 = Monday through 7 = Sunday. Note that the UI's {@code WEEKDAYS} constant uses
     * 0 = Sunday through 6 = Saturday, which agrees with this for Monday to Saturday but not
     * for Sunday. Going through {@link DayOfWeek} here keeps the assistant on the convention
     * the backend actually reads.
     */
    private static Integer isoDayOfWeek(DayOfWeek dayOfWeek) {
        return dayOfWeek.getValue();
    }

    private static void requireDate(LocalDate value, String field, String what) {
        if (value == null) {
            throw new DomainValidationException(
                "Set " + field + ": " + what + " needs a date. Ask the user for it rather than guessing.");
        }
    }

    private static void requireWeekday(DayOfWeek value, String recurrence) {
        if (value == null) {
            throw new DomainValidationException(
                "Set onWeekday: a " + recurrence + " item needs a day of the week.");
        }
    }

    private static void requireDayOfMonth(Integer value, String field, String what) {
        if (value == null || value < MIN_DAY || value > MAX_DAY) {
            throw new DomainValidationException(
                "Set " + field + " to a day between " + MIN_DAY + " and " + MAX_DAY + " for " + what + ".");
        }
    }

    private static void requireMonthDays(List<OnMonthDay> monthDays, int expected, String recurrence) {
        if (monthDays == null || monthDays.size() != expected) {
            throw new DomainValidationException(
                "A " + recurrence + " item needs exactly " + expected + " month/day pair"
                + (expected == 1 ? "" : "s") + " in onMonthDays. Ask the user which.");
        }
        for (OnMonthDay monthDay : monthDays) {
            if (monthDay == null || monthDay.month() == null || monthDay.day() == null) {
                throw new DomainValidationException("Every entry in onMonthDays needs both a month and a day.");
            }
            if (monthDay.month() < MIN_MONTH || monthDay.month() > MAX_MONTH) {
                throw new DomainValidationException("Months in onMonthDays must be between 1 and 12.");
            }
            if (monthDay.day() < MIN_DAY || monthDay.day() > MAX_DAY) {
                throw new DomainValidationException("Days in onMonthDays must be between 1 and 31.");
            }
        }
    }
}
