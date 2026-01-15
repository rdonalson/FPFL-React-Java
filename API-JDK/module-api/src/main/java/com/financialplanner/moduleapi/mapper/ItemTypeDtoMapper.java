package com.financialplanner.moduleapi.mapper;

import com.financialplanner.moduleapi.dto.itemtype.ItemTypeRequest;
import com.financialplanner.moduleapi.dto.itemtype.ItemTypeResponse;
import com.financialplanner.modulecommonbc.sanitizer.Sanitizer;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemType;
import org.springframework.stereotype.Component;

/**
 * The {@code ItemTypeDtoMapper} class provides methods for mapping between data transfer objects (DTOs)
 * and domain entities related to item types. This class is a component within the application's
 * service layer and handles the conversion of {@code ItemTypeRequest} and {@code ItemType}
 * during various operations.
 *
 * Key Responsibilities:
 * - Maps {@code ItemType} objects to {@code ItemTypeResponse} objects.
 * - Maps {@code ItemTypeRequest} objects to {@code ItemType} objects.
 * - Ensures sanitized data is used during mapping to comply with security and data validation requirements.
 *
 * This class requires a {@code Sanitizer} instance to be provided, which is used to sanitize the inputs
 * during mapping operations.
 */
@Component
public class ItemTypeDtoMapper {

    /**
     * Sanitizer instance used to sanitize inputs during the mapping process between DTOs and entities.
     * Ensures that sensitive or unsafe information within the provided objects is processed to conform
     * to security and data integrity requirements.
     * <ul>
     * Responsibilities:
     * - Removes or modifies undesirable and unsafe elements within objects.
     * - Processes nested or complex structures as needed.
     * - Applied to the data within the {@code ItemTypeDtoMapper}'s mapping logic.
     * Constraints:
     * - Must not be null.
     * - Any implementation of the {@code Sanitizer} interface can be injected at runtime.
     * Usage in {@code ItemTypeDtoMapper}:
     * - Called before mapping an {@code ItemTypeRequest} to an {@code ItemType}.
     * - Called when sanitizing an {@code ItemType} before converting it into an {@code ItemTypeResponse}.
     */
    private final Sanitizer sanitizer;

    /**
     * Constructs an instance of {@code ItemTypeDtoMapper} with the specified {@code Sanitizer}.
     *
     * @param sanitizer the sanitizer instance to be used for sanitizing inputs during
     *                  the mapping process between DTOs and entities. Must not be null.
     */
    public ItemTypeDtoMapper(Sanitizer sanitizer) {
        this.sanitizer = sanitizer;
    }

    /**
     * Converts the provided {@code ItemType} instance into an {@code ItemTypeResponse} object.
     * Before conversion, the input entity is sanitized to ensure it meets security and data validity requirements.
     *
     * @param domain the {@code ItemType} to be converted; must not be null
     * @return an instance of {@code ItemTypeResponse} containing the sanitized ID and name from the input entity
     */
    public ItemTypeResponse toResponse(ItemType domain) {
        sanitizer.sanitize(domain);
        return new ItemTypeResponse(domain.getId(), domain.getName());
    }

    /**
     * Converts an {@code ItemTypeRequest} object into an {@code ItemType} object.
     * This method performs sanitization on the provided {@code ItemTypeRequest}, ensuring
     * that the input adheres to security and data integrity requirements. It then maps
     * the sanitized {@code ItemTypeRequest} to a corresponding {@code ItemType}.
     *
     * @param request the {@code ItemTypeRequest} object containing the data to be mapped;
     *                must not be null, and its fields must adhere to validation constraints
     * @return an {@code ItemType} object created using the data from the sanitized
     *         {@code ItemTypeRequest}
     */
    public ItemType toEntity(ItemTypeRequest request) {
        sanitizer.sanitize(request);
        return new ItemType(request.id(), request.name());
    }
}
