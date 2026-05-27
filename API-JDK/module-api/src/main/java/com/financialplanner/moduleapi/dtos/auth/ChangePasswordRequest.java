package com.financialplanner.moduleapi.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
    @NotNull(message = "Record ID is required")
    Long id,

    @NotBlank(message = "Current Password is required")
    String currentPassword,

    @NotBlank(message = "New Password is required")
    @Size(min = 8, message = "New Password must be at least 8 characters")
    String newPassword
) {}

