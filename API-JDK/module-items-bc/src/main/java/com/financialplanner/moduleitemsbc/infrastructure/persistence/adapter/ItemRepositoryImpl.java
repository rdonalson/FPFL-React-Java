package com.financialplanner.moduleitemsbc.infrastructure.persistence.adapter;

import com.financialplanner.modulecommonbc.exception.DuplicateItemException;
import com.financialplanner.modulecommonbc.exception.ItemNotFoundException;
import com.financialplanner.modulecommonbc.exception.RepositoryException;
import com.financialplanner.moduleitemsbc.domain.repository.ItemRepository;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.repository.entity.JpaItemRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the {@link ItemRepository} interface.
 * This class provides concrete implementations for CRUD operations
 * and custom data access operations for managing {@link Item} entities.
 * It delegates the actual database interactions to the {@link JpaItemRepository}.
 * Responsibilities:
 * - Retrieve all {@link Item} entities.
 * - Fetch a specific {@link Item} by its unique identifier.
 * - Retrieve {@link Item} entities based on user ID and item type ID.
 * - Save a new or updated {@link Item} entity to the data source.
 * - Delete an {@link Item} entity by its unique identifier.
 * Exception Handling:
 * - Converts {@link DataAccessException} into domain-specific exceptions like
 * {@link ItemNotFoundException}, {@link DuplicateItemException}, and {@link RepositoryException}.
 * - Ensures that repository-level exceptions are properly wrapped to provide more contextual
 * error details to the application.
 */
@Component
public class ItemRepositoryImpl implements ItemRepository {

    private final JpaItemRepository jpa;

    // Constructor
    public ItemRepositoryImpl(JpaItemRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Item> findAll() {
        try {
            return jpa.findAll();
        } catch (DataAccessException ex) {
            throw new RepositoryException("Database failure while fetching Items", ex);
        }
    }

    @Override
    public Optional<Item> findById(Long id) {
        try {
            return jpa.findById(id);
        } catch (DataAccessException ex) {
            throw new RepositoryException("Database failure while fetching Items" + id, ex);
        }
    }

    @Override
    public List<Item> findByUserIdAndItemTypeId(UUID userId, Long itemTypeId) {
        try {
            return jpa.findByUserIdAndItemTypeId(userId, itemTypeId);
        } catch (DataAccessException ex) {
            throw new RepositoryException("Database failure while fetching Items", ex);
        }
    }

    @Override
    public Item save(Item entity) {
        try {
            return jpa.save(entity);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateItemException("Item already exists: " + entity.getId(), ex);
        } catch (DataAccessException ex) {
            throw new RepositoryException("Database failure while saving Item " + entity.getId(), ex);
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            if (!jpa.existsById(id)) {
                throw new ItemNotFoundException("Item " + id + " not found");
            }
            jpa.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new RepositoryException("Constraint violation while deleting Item " + id, ex);
        } catch (DataAccessException ex) {
            throw new RepositoryException("Database failure while deleting Item " + id, ex);
        }
    }
}

