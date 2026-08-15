package com.financialplanner.moduleapi.dtos.userroles;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ReplaceUserRolesRequest(
    @NotNull Long userId,
    @NotEmpty List<Long> roleIds
) {}
