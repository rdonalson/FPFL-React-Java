package com.financialplanner.moduleapi.dtos.initialamount;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemType;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Response DTO for InitialAmount entity.
 * Immutable record used to return InitialAmount data to clients.
 */
public record InitialAmountResponse(Long id,
                                    UUID userId,
                                    String name,
                                    Double amount,
                                    ItemType ItemType,
                                    LocalDate beginDate) {
}
