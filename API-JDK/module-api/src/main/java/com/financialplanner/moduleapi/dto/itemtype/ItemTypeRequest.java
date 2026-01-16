package com.financialplanner.moduleapi.dto.itemtype;

import com.financialplanner.modulecommonbc.exception.IllegalArgumentException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

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
