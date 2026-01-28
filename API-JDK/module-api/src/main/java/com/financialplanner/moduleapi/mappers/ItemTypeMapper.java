package com.financialplanner.moduleapi.mappers;

import com.financialplanner.moduleapi.dtos.itemtype.ItemTypeRequest;
import com.financialplanner.moduleapi.dtos.itemtype.ItemTypeResponse;
import com.financialplanner.modulecommonbc.sanitizer.Sanitizer;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemType;
import org.springframework.stereotype.Component;

/**
 * The ItemTypeMapper class is responsible for mapping between domain entities and DTOs
 * related to item types in the system. It converts ItemType entities to their
 * corresponding response DTOs (ItemTypeResponse) and vice versa, while ensuring that
 * the inputs are sanitized before processing.
 *
 * This class utilizes the {@code Sanitizer} to sanitize the input data and adheres to
 * the principle of clean data handling, ensuring that any potentially unsafe or invalid
 * input is appropriately cleaned before further operations.
 *
 * Responsibilities:
 * - Map a domain {@code ItemType} entity to {@code ItemTypeResponse}.
 * - Map a {@code ItemTypeRequest} DTO to a domain {@code ItemType} entity.
 * - Ensure input data to these methods is sanitized before further processing.
 *
 * Dependency:
 * - {@code Sanitizer}: A component that sanitizes inputs to ensure data conforms to constraints
 *   and prevents issues related to unsafe or invalid data.
 */
@Component
public class ItemTypeMapper {

    private final Sanitizer sanitizer;

    public ItemTypeMapper(Sanitizer sanitizer) {
        this.sanitizer = sanitizer;
    }

    public ItemTypeResponse toResponse(ItemType domain) {
        sanitizer.sanitize(domain);
        return new ItemTypeResponse(domain.getId(), domain.getName());
    }

    public ItemType toEntity(ItemTypeRequest request) {
        sanitizer.sanitize(request);
        return new ItemType(request.id(), request.name());
    }
}
