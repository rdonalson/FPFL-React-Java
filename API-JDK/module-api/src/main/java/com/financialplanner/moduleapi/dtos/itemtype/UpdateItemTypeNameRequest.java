package com.financialplanner.moduleapi.dtos.itemtype;

import com.financialplanner.modulecommonbc.exception.DomainValidationException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Represents a request object for updating the name of an item type.
 * This record encapsulates the necessary data to perform an update operation
 * on the name of an existing item type. Validation rules are enforced to ensure
 * that the provided name adheres to domain constraints.
 * Constraints:
 * - {@code name} must not be blank.
 * - {@code name} must not exceed 75 characters.
 * If the validation constraints are violated, a {@code DomainValidationException}
 * will be thrown during the construction of the object.
 *
 * @param name The updated name of the item type.
 */
public record UpdateItemTypeNameRequest(@NotBlank @Size(max = 75) String name) {
    public UpdateItemTypeNameRequest {
        if (name == null || name.isBlank()) {
            throw new DomainValidationException("ItemType Name is required and cannot be blank");
        }
    }
}
