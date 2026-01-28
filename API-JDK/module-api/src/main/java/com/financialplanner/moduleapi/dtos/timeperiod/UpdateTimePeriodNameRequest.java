package com.financialplanner.moduleapi.dtos.timeperiod;

import com.financialplanner.modulecommonbc.exception.DomainValidationException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Represents a request to update the name of a time period.
 * This record encapsulates the new name of the time period
 * and enforces validation constraints to ensure the validity
 * of the provided name.
 * Constraints:
 * - The name must not be null or blank.
 * - The name must not exceed 75 characters in length.
 * Validation:
 * - If the name is null or blank, a {@link DomainValidationException}
 * will be thrown with a message indicating the violation.
 */
public record UpdateTimePeriodNameRequest(@NotBlank @Size(max = 75) String name) {
    public UpdateTimePeriodNameRequest {
        if (name == null || name.isBlank()) {
            throw new DomainValidationException("TimePeriod Name is required and cannot be blank");
        }
    }
}
