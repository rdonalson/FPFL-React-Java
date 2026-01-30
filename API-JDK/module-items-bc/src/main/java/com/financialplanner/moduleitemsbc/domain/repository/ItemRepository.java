package com.financialplanner.moduleitemsbc.domain.repository;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing {@link Item} entities.
 * Provides methods to perform CRUD operations on {@link Item} objects.
 * This interface serves as a contract to be implemented by concrete
 * repository classes, defining the essential data access functionality.
 * Responsibilities:
 * - Retrieve all {@link Item} entities from the data source.
 * - Fetch a specific {@link Item} by its unique identifier.
 * - Save a new or updated {@link Item} into the data source.
 * - Delete an {@link Item} by its unique identifier.
 */
public interface ItemRepository {
    List<Item> findAll();
    Optional<Item> findById(Long id);
    List<Item> findByUserIdAndItemTypeId(UUID userId, Long itemTypeId);
    Item save(Item entity);
    void deleteById(Long id);
}
