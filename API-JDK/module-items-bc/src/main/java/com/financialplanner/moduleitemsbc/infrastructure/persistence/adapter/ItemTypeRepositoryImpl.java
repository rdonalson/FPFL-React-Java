package com.financialplanner.moduleitemsbc.infrastructure.persistence.adapter;

import com.financialplanner.modulecommonbc.exception.DuplicateItemException;
import com.financialplanner.modulecommonbc.exception.ItemNotFoundException;
import com.financialplanner.modulecommonbc.exception.RepositoryException;
import com.financialplanner.moduleitemsbc.domain.repository.ItemTypeRepository;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemType;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.repository.JpaItemTypeRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of the {@code ItemTypeRepository} interface, providing
 * CRUD (Create, Read, Update, Delete) functionality for managing {@code ItemType}
 * entities in the repository layer. This class acts as a bridge between the domain layer
 * and the persistence layer by delegating data access operations to the underlying
 * {@code JpaItemTypeRepository}.
 * This implementation handles specific scenarios such as data integrity violations,
 * entity duplication, or missing entities by throwing appropriate domain exceptions to ensure
 * consistent error handling and domain integrity.
 * Responsibilities of this class include:
 * - Fetching all {@code ItemType} records from the data source.
 * - Retrieving a specific {@code ItemType} by its identifier.
 * - Saving new or updated {@code ItemType} entities.
 * - Deleting {@code ItemType} entities by their identifier with appropriate validation.
 * Exception handling:
 * - {@code RepositoryException} is thrown for general database communication or
 * persistence layer failures.
 * - {@code ItemNotFoundException} is thrown when trying to delete a non-existent entity.
 * - {@code DuplicateItemException} is thrown upon encountering integrity violations
 * due to duplicate entities.
 */
@Component
public class ItemTypeRepositoryImpl implements ItemTypeRepository {

    private final JpaItemTypeRepository jpa;

    // Constructor
    public ItemTypeRepositoryImpl(JpaItemTypeRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<ItemType> findAll() {
        try {
            return jpa.findAll();
        } catch (DataAccessException ex) {
            throw new RepositoryException("Database failure while fetching ItemTypes", ex);
        }
    }

    @Override
    public Optional<ItemType> findById(Long id) {
        try {
            return jpa.findById(id);
        } catch (DataAccessException ex) {
            throw new RepositoryException("Database failure while fetching ItemType " + id, ex);
        }
    }

    public ItemType getReferenceById(Long id) {
        try {
            return jpa.getReferenceById(id);
        } catch (DataAccessException ex) {
            throw new RepositoryException("Database failure while fetching ItemType " + id, ex);
        }
    }

    @Override
    public ItemType save(ItemType entity) {
        try {
            return jpa.save(entity);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateItemException("ItemType already exists: " + entity.getId(), ex);
        } catch (DataAccessException ex) {
            throw new RepositoryException("Database failure while saving ItemType " + entity.getId(), ex);
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            if (!jpa.existsById(id)) {
                throw new ItemNotFoundException("ItemType " + id + " not found");
            }
            jpa.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new RepositoryException("Constraint violation while deleting ItemType " + id, ex);
        } catch (DataAccessException ex) {
            throw new RepositoryException("Database failure while deleting ItemType " + id, ex);
        }
    }
}

