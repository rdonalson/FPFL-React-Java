package com.financialplanner.moduleapi.dtos.auth;

public record LoginRequest(
    String email,
    String password
) {}
