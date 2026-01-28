package com.financialplanner.moduleapi.mappers;

import com.financialplanner.moduleapi.dtos.itemtype.ItemTypeRequest;
import com.financialplanner.moduleapi.dtos.itemtype.ItemTypeResponse;
import com.financialplanner.modulecommonbc.sanitizer.Sanitizer;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemType;
import org.springframework.stereotype.Component;

/**
 * Maps between ItemType entity and its corresponding request and response DTOs.
 * This class is responsible for converting ItemType entities to ItemTypeResponse DTOs
 * and vice versa. It applies data sanitization before performing the conversion
 * to ensure input and output data meet defined constraints.
 * Responsibilities:
 * - Convert an ItemType entity to a corresponding ItemTypeResponse DTO.
 * - Convert an ItemTypeRequest DTO to a corresponding ItemType entity.
 * - Sanitize input objects before processing using the Sanitizer component.
 * Dependencies:
 * - Sanitizer: Used to sanitize data for both entities and DTOs to ensure integrity.
 * Thread Safety:
 * - Instances of this class are thread-safe as long as the provided Sanitizer
 * implementation is thread-safe.
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
