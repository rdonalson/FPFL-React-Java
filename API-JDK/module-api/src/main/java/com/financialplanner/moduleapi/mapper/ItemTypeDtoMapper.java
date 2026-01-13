package com.financialplanner.moduleapi.mapper;

import com.financialplanner.moduleapi.dto.itemtype.ItemTypeRequest;
import com.financialplanner.moduleapi.dto.itemtype.ItemTypeResponse;
import com.financialplanner.modulecommonbc.sanitizer.Sanitizer;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemTypeEntity;
import org.springframework.stereotype.Component;

/**
 * Provides functionality for mapping data between the {@code ItemTypeDto} and {@code ItemTypeEntity} objects.
 * This class acts as a bridge to convert the ItemType DTO, used in the service or controller layers,
 * into the ItemType entity, required for persistence in the database layer.
 * The mapping process ensures that all required attributes are transferred and validated
 * during the conversion between these two distinct object representations.
 * <p>
 * This class is annotated with {@code @Component} making it a Spring-managed bean that can be easily
 * injected into other components or services.
 */
@Component
public class ItemTypeDtoMapper {

    /**
     * A {@code Sanitizer} instance used to clean and validate objects during data mapping
     * processes. It ensures that the object being processed conforms to expected safety and
     * structural standards by removing or modifying any undesirable or unsafe elements.
     * The sanitizer is invoked during both entity-to-DTO and DTO-to-entity transformations
     * to maintain data integrity and security.
     * ---
     * Characteristics:
     * - Final: Ensures immutability of the reference throughout the lifetime of the containing class.
     * - Dependency: Injected through the constructor of the {@code ItemTypeDtoMapper} class,
     * allowing for configurable or testable behavior.
     */
    private final Sanitizer sanitizer;

    /**
     * Constructs a new instance of {@code ItemTypeDtoMapper} with the specified {@code Sanitizer}.
     *
     * @param sanitizer the sanitizer instance used to sanitize objects during the mapping process.
     *                  This is particularly useful for ensuring data security and conformance.
     *                  Must not be null.
     */
    public ItemTypeDtoMapper(Sanitizer sanitizer) {
        this.sanitizer = sanitizer;
    }

    /**
     * Converts the given {@code ItemTypeEntity} into an {@code ItemTypeResponse}.
     * The conversion process includes sanitizing the input entity to ensure its data is safe.
     *
     * @param domain the {@code ItemTypeEntity} instance to be converted, must not be null
     * @return a new {@code ItemTypeResponse} instance containing the ID and name from the input entity
     */
    public ItemTypeResponse toResponse(ItemTypeEntity domain) {
        sanitizer.sanitize(domain);
        return new ItemTypeResponse(domain.getId(), domain.getName());
    }

    /**
     * Converts an {@code ItemTypeRequest} object into an {@code ItemTypeEntity} object.
     * The fields from the request object are transferred to the corresponding fields
     * in the entity object. Additionally, the input request is sanitized before the
     * conversion to ensure data integrity and security.
     *
     * @param request the {@code ItemTypeRequest} object containing the data to be mapped
     *                to an {@code ItemTypeEntity}. This must not be null and must
     *                contain valid data for the {@code id} and {@code name} fields.
     * @return an {@code ItemTypeEntity} object populated with the sanitized data
     * from the {@code ItemTypeRequest}.
     */
    public ItemTypeEntity toEntity(ItemTypeRequest request) {
        sanitizer.sanitize(request);
        return new ItemTypeEntity(request.id(), request.name());
    }
}
