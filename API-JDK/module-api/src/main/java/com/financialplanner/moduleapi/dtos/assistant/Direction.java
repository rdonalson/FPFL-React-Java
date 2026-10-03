package com.financialplanner.moduleapi.dtos.assistant;

/**
 * Direction of money for an item the assistant drafts.
 * <p>
 * Maps onto the {@code fpfl.item_types} table: 1 = Credit, 2 = Debit. Amounts are always
 * recorded as positive numbers; this enum carries the sign semantics instead.
 * <p>
 * Exposed to the language model as a JSON-schema enum, so the model can only ever return
 * one of these two constants.
 */
public enum Direction {

    /** Money coming in: salary, refunds, interest. */
    CREDIT(1),

    /** Money going out: rent, bills, subscriptions. */
    DEBIT(2);

    private final int fkItemType;

    Direction(int fkItemType) {
        this.fkItemType = fkItemType;
    }

    /** The {@code fpfl.item_types} id this direction maps to. */
    public int fkItemType() {
        return fkItemType;
    }
}
