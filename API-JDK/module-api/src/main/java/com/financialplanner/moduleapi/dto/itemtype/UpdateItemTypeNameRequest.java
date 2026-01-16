package com.financialplanner.moduleapi.dto.itemtype;

import com.financialplanner.modulecommonbc.exception.IllegalArgumentException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateItemTypeNameRequest(
    @NotBlank
    @Size(max = 75)
    String name) {
    public UpdateItemTypeNameRequest {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required and cannot be blank");
        }
    }
}
