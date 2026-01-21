package com.financialplanner.moduleitemsbc.infrastructure.persistence.adapter;

import com.financialplanner.modulecommonbc.exception.DuplicateItemException;
import com.financialplanner.modulecommonbc.exception.ItemNotFoundException;
import com.financialplanner.modulecommonbc.exception.RepositoryException;
import com.financialplanner.moduleitemsbc.domain.repository.ItemTypeRepository;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemType;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.repository.JpaItemTypeRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of the {@code ItemTypeRepository} interface, providing the
 * concrete logic for managing {@code ItemType} entities in the persistence
 * layer using JPA.
 *
 * This class serves as an adapter that bridges the {@code ItemTypeRepository}
 * contract with the {@code JpaItemTypeRepository} interface, allowing higher
 * layers of the application to interact with {@code ItemType} entities
 * without directly depending on the underlying persistence framework.
 *
 * Features:
 * - Handles CRUD (Create, Read, Update, Delete) operations for {@code ItemType} entities.
 * - Wraps JPA exceptions into domain-specific exceptions, including:
 *   - {@code DuplicateItemException}: Thrown when attempting to save an entity that violates
 *     uniqueness constraints.
 *   - {@code RepositoryException}: Thrown for general repository-related failures
 *     such as database connectivity issues or unexpected exceptions.
 *
 * Dependencies:
 * - {@code JpaItemTypeRepository}: Defines the persistence operations for
 *   {@code ItemType} entities and integrates with the JPA infrastructure.
 *
 * Thread Safety:
 * - Instances of this class are thread-safe only if all dependencies are thread-safe.
 *
 * Constructor:
 * - Accepts an instance of {@code JpaItemTypeRepository}, injected via dependency
 *   injection, and serves as the data access layer.
 */
@Repository
public class ItemTypeRepositoryImpl implements ItemTypeRepository {

    private final JpaItemTypeRepository jpa;

    public ItemTypeRepositoryImpl(JpaItemTypeRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public ItemType save(ItemType itemType) {
        try {
            return jpa.save(itemType);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateItemException("Item already exists: " + itemType.getId(), ex);
        } catch (DataAccessException ex) {
            throw new RepositoryException("Database failure while saving item " + itemType.getId(), ex);
        }
    }

    @Override
    public Optional<ItemType> findById(Long id) {
        try {
            return jpa.findById(id);
        } catch (DataAccessException ex) {
            throw new RepositoryException("Database failure while fetching item " + id, ex);
        }
    }

    @Override
    public List<ItemType> findAll() {
        return jpa.findAll();
    }

    @Override
    public void deleteById(Long id) {
        try {
            if (!jpa.existsById(id)) {
                throw new ItemNotFoundException("ItemType " + id + " not found");
            }
            jpa.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new RepositoryException("Constraint violation while deleting item " + id, ex);
        } catch (DataAccessException ex) {
            throw new RepositoryException("Database failure while deleting item " + id, ex);
        }
    }
}

