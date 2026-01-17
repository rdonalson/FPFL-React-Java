package com.financialplanner.moduleitemsbc.domain.repository;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemType;

import java.util.List;
import java.util.Optional;

/**
 * Interface representing the repository for managing {@code ItemType} entities.
 * Provides CRUD (Create, Read, Update, Delete) operations for persisting and retrieving
 * {@code ItemType} data from the underlying data source.
 *
 * This interface acts as a contract for interacting with {@code ItemType} entities,
 * abstracting the data persistence layer and allowing for domain-driven data operations.
 */
public interface ItemTypeRepository {
    ItemType save(ItemType entity);
    Optional<ItemType> findById(Long id);
    List<ItemType> findAll();
    void deleteById(Long id);
}
