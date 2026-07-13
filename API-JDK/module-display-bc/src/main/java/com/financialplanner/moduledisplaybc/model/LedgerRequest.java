package com.financialplanner.moduledisplaybc.model;

import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.UUID;

public record LedgerRequest(
    UUID userId,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ledgerStartDate,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ledgerEndDate,
    boolean includeGrouping
) {}
