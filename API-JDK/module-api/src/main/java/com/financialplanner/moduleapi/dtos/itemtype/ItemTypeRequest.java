package com.financialplanner.moduleapi.dtos.itemtype;

import com.financialplanner.modulecommonbc.exception.DomainValidationException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Represents a request object for creating or updating an item type.
 * This record encapsulates the necessary data to define an item type, which
 * includes its unique identifier and name. Validation rules are enforced on
 * the fields to ensure they adhere to the domain constraints.
 * Constraints:
 * - {@code id}: Must not be null and must be a positive integer.
 * - {@code name}: Must not be blank and must not exceed 75 characters.
 * If any validation constraints are violated, a {@code DomainValidationException}
 * will be thrown during the construction of the object.
 *
 * @param id   The unique identifier of the item type.
 * @param name The name of the item type.
 */
public record ItemTypeRequest(Long id, @NotBlank @Size(max = 75) String name) {

    public ItemTypeRequest {
        if (id == null || id <= 0) {
            throw new DomainValidationException("ItemType Id is required and must be a positive integer");
        }
        if (name == null || name.isBlank()) {
            throw new DomainValidationException("ItemType Name is required and cannot be blank");
        }
    }
}
