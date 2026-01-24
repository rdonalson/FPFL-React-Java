package com.financialplanner.moduleapi.dto.itemtype;

import com.financialplanner.modulecommonbc.exception.DomainValidationException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request object for updating the name of an item type.
 * This record encapsulates the necessary data to update the name of an existing item type.
 * It enforces non-blank constraints and a maximum character limit on the provided name.
 * Validation rules:
 * - {@code name} must not be blank.
 * - {@code name} must not exceed 75 characters.
 * If the validation constraints are violated, an {@code DomainValidationException} will
 * be thrown at the time of object construction.
 *
 * @param name The new name for the item type.
 */
public record UpdateItemTypeNameRequest(@NotBlank @Size(max = 75) String name) {
    public UpdateItemTypeNameRequest {
        if (name == null || name.isBlank()) {
            throw new DomainValidationException("ItemType Name is required and cannot be blank");
        }
    }
}
