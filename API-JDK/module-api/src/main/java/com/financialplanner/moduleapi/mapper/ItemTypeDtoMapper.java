package com.financialplanner.moduleapi.mapper;

import com.financialplanner.moduleapi.dto.ItemTypeDto;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemTypeEntity;
import org.springframework.stereotype.Component;

/**
 * A mapper class responsible for converting between {@code ItemTypeEntity} and {@code ItemTypeDto}.
 * This class provides utility methods for transforming entities to DTOs and vice versa, ensuring
 * a clean separation between the persistence layer and the application layer.
 * This class is typically used in service or controller layers where such conversions are necessary.
 * By using this mapper, we can simplify data transformation and reduce duplication of mapping logic
 * across the application.
 * The class is annotated with {@code @Component}, allowing Spring to manage its lifecycle and dependency injection.
 */
@Component
public class ItemTypeDtoMapper {
    /**
     * Converts an {@code ItemTypeEntity} instance to an {@code ItemTypeDto} instance.
     * This method extracts the ID and name from the given entity and maps them
     * to a new DTO object.
     * -----------
     *
     * @param entity the {@code ItemTypeEntity} instance to be converted; must not be null
     * @return an {@code ItemTypeDto} instance populated with data from the given entity
     */
    public ItemTypeDto toDomain(ItemTypeEntity entity) {
        return new ItemTypeDto(entity.getId(),
                               entity.getName()
        );
    }

    /**
     * Converts an {@code ItemTypeDto} object to an {@code ItemTypeEntity} object.
     * This method facilitates the transformation from the data transfer object
     * (DTO) format, which is typically used for communication between layers or
     * across system boundaries, to the entity format used within the persistence layer.
     *
     * @param domain the {@code ItemTypeDto} instance to be converted; must not be null.
     *               -----------            The {@code domain} parameter contains the ID and name of the item type.
     * @return an {@code ItemTypeEntity} object representing the same data as the provided {@code ItemTypeDto}.
     */
    public ItemTypeEntity toEntity(ItemTypeDto domain) {
        return new ItemTypeEntity(domain.getId(),
                                  domain.getName()
        );
    }
}
