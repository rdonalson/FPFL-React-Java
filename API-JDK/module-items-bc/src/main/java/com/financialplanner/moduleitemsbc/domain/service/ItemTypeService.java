package com.financialplanner.moduleitemsbc.domain.service;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemType;

import java.util.List;

/**
 * Service interface for managing {@code ItemType} entities.
 * This interface defines the operations for creating, retrieving, updating,
 * deleting, and listing {@code ItemType} entities. It acts as an abstraction
 * for the business layer to interact with the underlying persistence layer.
 * Responsibilities:
 * - Providing a contract for operations on {@code ItemType}.
 * - Enforcing application-level constraints for data integrity.
 * - Ensuring valid input for all operations.
 * Methods:
 * - {@code list()}: Retrieves a list of all {@code ItemType} entities.
 * - {@code get(Long id)}: Retrieves a specific {@code ItemType} by its identifier.
 * - {@code create(ItemType entity)}: Creates a new {@code ItemType} entity in the system.
 * - {@code update(ItemType entity)}: Updates the details of an existing {@code ItemType}.
 * - {@code delete(Long id)}: Deletes a specific {@code ItemType} by its identifier.
 * Exceptions expected to be handled (not included in the interface):
 * - Validation exceptions for invalid inputs.
 * - Exceptions when entities cannot be found or when duplicates are attempted to be created.
 */
public interface ItemTypeService {
    List<ItemType> list();
    ItemType get(Long id);
    ItemType create(ItemType entity);
    ItemType update(ItemType entity);
    void delete(Long id);
}
