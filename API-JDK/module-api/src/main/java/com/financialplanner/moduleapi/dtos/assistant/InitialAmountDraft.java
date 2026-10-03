package com.financialplanner.moduleapi.dtos.assistant;

/**
 * A proposed starting balance, drafted by the assistant and awaiting the user's review.
 * <p>
 * Unlike a credit or a debit, this amount may legitimately be negative: an overdrawn
 * account is a valid starting position.
 *
 * @param amount the starting balance
 */
public record InitialAmountDraft(Double amount) {}
