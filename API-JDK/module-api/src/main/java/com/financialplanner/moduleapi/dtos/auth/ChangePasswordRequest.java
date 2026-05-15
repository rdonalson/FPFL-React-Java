package com.financialplanner.moduleapi.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
    @NotBlank(message = "Current Password is required")
    String currentPassword,

    @NotBlank(message = "New Password is required")
    @Size(min = 8, message = "New Password must be at least 8 characters")
    String newPassword
) {}

