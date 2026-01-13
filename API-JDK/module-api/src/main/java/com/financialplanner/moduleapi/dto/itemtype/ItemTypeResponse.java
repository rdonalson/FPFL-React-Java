package com.financialplanner.moduleapi.dto.itemtype;

import jakarta.validation.constraints.NotNull;

public record ItemTypeResponse(
    @NotNull Long id,
    @NotNull String name
) {}

