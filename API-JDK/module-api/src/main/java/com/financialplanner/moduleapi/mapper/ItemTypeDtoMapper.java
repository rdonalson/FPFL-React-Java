package com.financialplanner.moduleapi.mapper;

import com.financialplanner.moduleapi.dto.ItemTypeDto;
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
     * Converts an instance of {@code ItemTypeDto} to an instance of {@code ItemTypeEntity}.
     *
     * @param domain the {@code ItemTypeDto} object to be converted; must not be null
     * @return a new {@code ItemTypeEntity} object containing the data from the provided {@code ItemTypeDto}
     */
    public ItemTypeEntity toEntity(ItemTypeDto domain) {
        return new ItemTypeEntity(domain.getId(), domain.getName());
    }
}
