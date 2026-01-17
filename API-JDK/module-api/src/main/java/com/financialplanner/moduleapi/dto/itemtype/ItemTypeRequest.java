package com.financialplanner.moduleapi.dto.itemtype;

import com.financialplanner.modulecommonbc.exception.IllegalArgumentException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Represents a request object for creating or processing an item type.
 *
 * This record encapsulates the necessary data to define an item type, including its unique
 * identifier and name. Validation rules enforce constraints on the fields to ensure the integrity
 * of the data before further processing.
 *
 * Validation rules:
 * - {@code id} must not be null.
 * - {@code name} must not be blank.
 * - {@code name} must not exceed 75 characters.
 *
 * If the validation constraints are violated, an {@code IllegalArgumentException} will be thrown
 * during object construction.
 *
 * @param id The unique identifier for the item type.
 * @param name The name of the item type.
 */
public record ItemTypeRequest(
    Long id,
    @NotBlank
    @Size(max = 75)
    String name) {

    public ItemTypeRequest {
        if (id == null) {
            throw new IllegalArgumentException("Id is required and cannot be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required and cannot be blank");
        }
    }
}
