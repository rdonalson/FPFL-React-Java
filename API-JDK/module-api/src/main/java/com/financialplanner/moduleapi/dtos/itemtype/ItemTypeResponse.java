package com.financialplanner.moduleapi.dtos.itemtype;

import jakarta.validation.constraints.NotNull;

/**
 * Represents a response object for an item type.
 * This record encapsulates the details of an item type, including its unique identifier and name.
 * All fields are mandatory and validated to ensure they are not null.
 * Fields:
 * - {@code id}: The unique identifier of the item type.
 * - {@code name}: The name of the item type.
 */
public record ItemTypeResponse(
    @NotNull Long id,
    @NotNull String name
) {}

