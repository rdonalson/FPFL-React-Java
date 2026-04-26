package com.financialplanner.moduleapi.dtos.initialamount;

import com.financialplanner.modulecommonbc.exception.DomainValidationException;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO used to create or update an InitialAmount record.
 * Validation rules mirror the ItemRequest pattern: required userId, name and positive amount.
 */
public record InitialAmountRequest(@NotNull UUID userId,
                                   @NotNull String name,
                                   @NotNull Double amount,
                                   @NotNull Integer fkItemType,
                                   LocalDate beginDate
) {

    public InitialAmountRequest {
        if (userId == null) {
            throw new DomainValidationException("InitialAmount UserId is required and cannot be blank");
        }
        if (amount == null) {
            throw new DomainValidationException("InitialAmount Amount is required and cannot be null");
        }
    }
}
