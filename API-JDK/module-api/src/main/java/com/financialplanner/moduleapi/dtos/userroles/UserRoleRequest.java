package com.financialplanner.moduleapi.dtos.userroles;

import jakarta.validation.constraints.NotNull;

public record UserRoleRequest(
    @NotNull Long userId,
    @NotNull Long roleId
) {}
