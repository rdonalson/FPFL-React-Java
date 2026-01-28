package com.financialplanner.moduleapi.dtos.timeperiod;

import com.financialplanner.modulecommonbc.exception.DomainValidationException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateTimePeriodNameRequest(@NotBlank @Size(max = 75) String name) {
    public UpdateTimePeriodNameRequest {
        if (name == null || name.isBlank()) {
            throw new DomainValidationException("TimePeriod Name is required and cannot be blank");
        }
    }
}
