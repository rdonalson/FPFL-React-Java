package com.financialplanner.moduleitemsbc.domain.service;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemType;

import java.util.List;

/**
 * Interface for managing {@code ItemType} entities.
 *
 * This service provides the contract for CRUD operations related to
 * {@code ItemType} objects. It defines methods for creating, updating,
 * retrieving, listing, and deleting {@code ItemType} entities,
 * enabling interaction with the underlying data store. Implementing classes
 * are responsible for ensuring transactional boundaries and any necessary
 * business logic while performing these operations.
 */
public interface ItemTypeService {
    ItemType create(ItemType entity);
    ItemType update(ItemType entity);
    List<ItemType> list();
    ItemType get(Long id);
    void delete(Long id);
}
