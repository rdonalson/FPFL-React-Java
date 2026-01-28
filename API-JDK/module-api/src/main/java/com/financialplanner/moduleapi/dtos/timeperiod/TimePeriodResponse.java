package com.financialplanner.moduleapi.dtos.timeperiod;

import jakarta.validation.constraints.NotNull;

/**
 * Represents the response object for a time period.
 * This class encapsulates the essential data of a time period,
 * including its unique identifier and name.
 * Constraints:
 * - The {@code id} must not be {@code null}.
 * - The {@code name} must not be {@code null}.
 * It is typically used as a data structure for sending time period
 * information from the server to the client.
 * Validation annotations enforce non-null constraints:
 * - {@link NotNull} ensures that
 * the {@code id} and {@code name} fields are provided.
 */
public record TimePeriodResponse(@NotNull Long id, @NotNull String name) {}
