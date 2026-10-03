package com.financialplanner.moduleapi.dtos.assistant;

/**
 * How often an assistant-drafted item occurs.
 * <p>
 * Each constant carries the {@code fpfl.time_periods} id that the recurrence expanders in
 * {@code module-display-bc} filter on, so the mapping between a model-friendly name and a
 * database key lives in exactly one place.
 * <p>
 * Exposed to the language model as a JSON-schema enum. The model cannot invent
 * "fortnightly" or "every other Friday-ish"; it must choose one of these.
 */
public enum Recurrence {

    /** A single dated occurrence. */
    ONE_TIME(1),

    /** Every day in range. */
    DAILY(2),

    /** Weekly on a chosen weekday. */
    WEEKLY(3),

    /** Every other week on a chosen weekday. */
    EVERY_TWO_WEEKS(4),

    /** Twice a month on two chosen days of the month. */
    BI_MONTHLY(5),

    /** Monthly on a chosen day of the month. */
    MONTHLY(6),

    /** Four times a year on chosen month/day pairs. */
    QUARTERLY(7),

    /** Twice a year on chosen month/day pairs. */
    SEMI_ANNUAL(8),

    /** Once a year on a chosen month/day. */
    ANNUAL(9),

    /** The nth (or last) weekday of each month. */
    NTH_WEEKDAY(10);

    private final int fkPeriod;

    Recurrence(int fkPeriod) {
        this.fkPeriod = fkPeriod;
    }

    /** The {@code fpfl.time_periods} id this recurrence maps to. */
    public int fkPeriod() {
        return fkPeriod;
    }
}
