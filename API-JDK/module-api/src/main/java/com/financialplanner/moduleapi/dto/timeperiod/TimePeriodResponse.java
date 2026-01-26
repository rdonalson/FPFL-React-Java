package com.financialplanner.moduleapi.dto.timeperiod;

import jakarta.validation.constraints.NotNull;

public record TimePeriodResponse(
    @NotNull Long id,
    @NotNull String name
) {}
