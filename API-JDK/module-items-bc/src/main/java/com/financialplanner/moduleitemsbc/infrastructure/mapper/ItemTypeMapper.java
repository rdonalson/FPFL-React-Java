package com.financialplanner.moduleitemsbc.infrastructure.mapper;

import com.financialplanner.moduleitemsbc.domain.entity.ItemTypeEntity;
import com.financialplanner.moduleitemsbc.domain.model.ItemType;
import org.springframework.stereotype.Component;

/**
 * A mapper class responsible for converting between {@code ItemTypeEntity}, which represents
 * the persistence model, and {@code ItemType}, which represents the domain model.
 * This class facilitates transformations to ensure seamless communication between
 * the persistence and domain layers of the application.
 * ---------------
 * Responsibilities:
 * - Converts an {@link ItemTypeEntity} instance retrieved from the database into its
 *   corresponding domain object {@link ItemType}.
 * - Converts a domain object {@link ItemType} into its persistence-layer entity
 *   {@link ItemTypeEntity}, to be saved or updated in the database.
 * --------------
 * This class is a Spring {@code @Component}, allowing it to be auto-detected and
 * injected where needed in the application's dependency graph.
 */
@Component
public class ItemTypeMapper {
    /**
     * Converts an {@link ItemTypeEntity} instance into its corresponding domain model {@link ItemType}.
     * This method maps the attributes of the given entity to the domain object, enabling
     * seamless transformation between persistence and domain layers.
     *
     * @param entity the {@link ItemTypeEntity} object to be converted; must not be null
     * @return an {@link ItemType} representing the domain model equivalent of the provided entity
     */
    public ItemType toDomain(ItemTypeEntity entity) {
        return new ItemType(
            entity.getId(),
            entity.getName()
        );
    }

    /**
     * Converts a domain object of type {@code ItemType} into its corresponding
     * entity object of type {@code ItemTypeEntity}.
     *
     * @param domain the {@code ItemType} object to be converted; must not be null
     * @return the converted {@code ItemTypeEntity} object, instantiated with the
     *         ID and name from the provided {@code ItemType} object
     */
    public ItemTypeEntity toEntity(ItemType domain) {
        return new ItemTypeEntity(
            domain.getId(),
            domain.getName()
        );
    }
}
