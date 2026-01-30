package com.financialplanner.moduleitemsbc.domain.repository;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemType;

import java.util.List;
import java.util.Optional;

/**
 * Provides an abstraction for managing {@code ItemType} entities within the repository layer.
 * This interface defines the standard CRUD (Create, Read, Update, Delete) operations as well as
 * operations to retrieve an entity reference by its identifier.
 * Responsibilities:
 * - Retrieve all {@code ItemType} entities.
 * - Retrieve a specific {@code ItemType} entity by its ID.
 * - Retrieve a reference to an {@code ItemType} entity without fully loading it.
 * - Save a new or updated {@code ItemType} entity.
 * - Delete an {@code ItemType} entity by its ID.
 * Usage:
 * - This interface is used by higher-level service layers or components that require
 * access to {@code ItemType} data and operations.
 * Thread Safety:
 * - Implementations of this interface should ensure thread safety if used in a
 * multi-threaded environment.
 * Exceptions:
 * - Implementations may throw database or persistence-related exceptions for operations
 * such as retrieval, saving, or deleting entities.
 */
public interface ItemTypeRepository {
    List<ItemType> findAll();
    Optional<ItemType> findById(Long id);
    ItemType getReferenceById(Long id);
    ItemType save(ItemType entity);
    void deleteById(Long id);
}
