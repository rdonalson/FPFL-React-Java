package com.financialplanner.moduleapi.dto.timeperiod;

import com.financialplanner.modulecommonbc.exception.DomainValidationException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

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
