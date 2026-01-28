package com.financialplanner.moduleitemsbc.infrastructure.persistence.adapter;

import com.financialplanner.modulecommonbc.exception.DuplicateItemException;
import com.financialplanner.modulecommonbc.exception.ItemNotFoundException;
import com.financialplanner.modulecommonbc.exception.RepositoryException;
import com.financialplanner.moduleitemsbc.domain.repository.ItemRepository;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.repository.JpaItemRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of the {@link ItemRepository} interface that provides
 * data access logic for {@link Item} entities. This class acts as a bridge
 * between the service layer and the database repository layer.
 * This implementation uses {@link JpaItemRepository} as the underlying
 * repository to interact with the database and handles exception scenarios
 * for repository operations.
 * Responsibilities:
 * - Fetch all {@link Item} entities from the database.
 * - Retrieve a single {@link Item} entity by its ID.
 * - Save a new or updated {@link Item} entity to the database.
 * - Delete an {@link Item} entity by its ID.
 * Exception Handling:
 * - Wraps {@link DataAccessException} into custom exceptions where appropriate.
 * - Throws {@link RepositoryException} for generic database-related errors.
 * - Throws {@link DuplicateItemException} when attempting to save a duplicate {@link Item}.
 * - Throws {@link ItemNotFoundException} when attempting to delete a non-existent {@link Item}.
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
            throw new RepositoryException("Database failure while fetching ItemTypes", ex);
        }
    }

    @Override
    public Optional<Item> findById(Long id) {
        try {
            return jpa.findById(id);
        } catch (DataAccessException ex) {
            throw new RepositoryException("Database failure while fetching ItemType " + id, ex);
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

