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
 * Implementation of the {@code ItemTypeRepository} interface that provides
 * methods to perform CRUD operations on {@code ItemType} entities.
 * This class uses a JPA repository ({@code JpaItemTypeRepository}) for
 * data access and introduces exception handling to manage
 * database access issues and integrity constraints in the domain context.
 * Methods in this class perform the following operations:
 * - Retrieve all {@code ItemType} entities from the database
 * - Retrieve an {@code ItemType} by its unique identifier
 * - Obtain a reference to an {@code ItemType} without initialization
 * - Save a new or updated {@code ItemType} into the database
 * - Delete an {@code ItemType} by its unique identifier
 * Custom exceptions ({@code RepositoryException}, {@code DuplicateItemException},
 * and {@code ItemNotFoundException}) are thrown to encapsulate database errors,
 * constraint violations, and cases where specific items are not found.
 * This class is annotated with {@code @Component} to enable Spring's
 * component-scanning mechanism to detect and register it as a bean.
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

    /**
     * Retrieves a reference to the {@code ItemType} entity identified by the given ID
     * without fully initializing or loading it from the database.
     * @param id the unique identifier of the {@code ItemType} to retrieve
     * @return a reference to the {@code ItemType} if found
     * @throws RepositoryException if a database access error occurs while fetching the entity
     */
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

