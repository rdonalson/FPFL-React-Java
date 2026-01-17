package com.financialplanner.moduleapi.mapper;

import com.financialplanner.moduleapi.dto.itemtype.ItemTypeRequest;
import com.financialplanner.moduleapi.dto.itemtype.ItemTypeResponse;
import com.financialplanner.modulecommonbc.sanitizer.Sanitizer;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemType;
import org.springframework.stereotype.Component;

/**
 * A mapper class responsible for converting between {@code ItemType} domain objects,
 * {@code ItemTypeRequest} objects, and {@code ItemTypeResponse} objects.
 *
 * This class is used to facilitate transformations in both directions:
 * - From domain objects to response DTOs for API communication.
 * - From request DTOs to domain objects for persistence or further processing.
 *
 * The class utilizes a {@code Sanitizer} to ensure objects are properly sanitized
 * before conversion to mitigate potential security risks or ensure data integrity.
 *
 * Primary responsibilities:
 * - Transform a {@code ItemType} domain entity into a {@code ItemTypeResponse} DTO.
 * - Transform a {@code ItemTypeRequest} DTO into a {@code ItemType} domain entity.
 *
 * Dependencies:
 * - {@code Sanitizer}: Used to clean and validate objects before processing.
 *
 * Thread Safety:
 * This class is marked with {@code @Component} and should be used in a Spring application
 * context, ensuring thread safety through the container's management of bean instances.
 */
@Component
public class ItemTypeDtoMapper {

    private final Sanitizer sanitizer;

    public ItemTypeDtoMapper(Sanitizer sanitizer) {
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
