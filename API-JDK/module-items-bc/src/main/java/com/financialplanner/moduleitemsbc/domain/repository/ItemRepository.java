package com.financialplanner.moduleitemsbc.domain.repository;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing {@code Item} entities.
 * Provides methods for CRUD operations and custom query methods
 * to interact with the underlying data source.
 */
public interface ItemRepository {
    List<Item> findAll();
    Optional<Item> findById(Long id);
    List<Item> findByUserIdAndItemTypeId(UUID userId, Long itemTypeId);
    List<Item> findByUserId(UUID userId);
    Item save(Item entity);
    void deleteById(Long id);
}
