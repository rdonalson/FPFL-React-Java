package com.financialplanner.moduleitemsbc.domain.service;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemType;

import java.util.List;

/**
 * Service interface for managing {@code ItemType} entities.
 * This interface provides methods for performing CRUD (Create, Read, Update, Delete) operations
 * and listing all {@code ItemType} entities. It defines the contract for interacting
 * with the underlying data repository that stores {@code ItemType} objects.
 *
 * The typical responsibilities of this service include:
 * - Persisting new {@code ItemType} entities.
 * - Updating existing {@code ItemType} entities.
 * - Retrieving specific {@code ItemType} entities by their unique identifier.
 * - Listing all existing {@code ItemType} entities.
 * - Deleting specific {@code ItemType} entities by their unique identifier.
 */
public interface ItemTypeService {
    ItemType create(ItemType entity);

    ItemType update(ItemType entity);

    List<ItemType> list();

    ItemType get(Long id);

    void delete(Long id);
}
