package com.financialplanner.moduleapi.dtos.timeperiod;

import com.financialplanner.modulecommonbc.exception.DomainValidationException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Represents a request object for a time period.
 * This record class encapsulates the necessary data for creating or referencing a time period,
 * which includes its unique identifier and its name.
 * Constraints:
 * - The {@code id} must not be null and must be a positive integer.
 * - The {@code name} must not be null, blank, or exceed 75 characters in length.
 * Validation:
 * - If the {@code id} is null or not a positive integer, a {@link DomainValidationException}
 * will be thrown with an appropriate error message.
 * - If the {@code name} is null or blank, a {@link DomainValidationException} will be thrown
 * detailing the constraint violation.
 * This class ensures that the data provided for a time period request is consistent with the
 * domain rules and constraints before proceeding with any operations.
 */
public record TimePeriodRequest(Long id, @NotBlank @Size(max = 75) String name) {

    public TimePeriodRequest {
        if (id == null || id <= 0) {
            throw new DomainValidationException("TimePeriod Id is required and must be a positive integer");
        }
        if (name == null || name.isBlank()) {
            throw new DomainValidationException("TimePeriod Name is required and cannot be blank");
        }
    }
}
