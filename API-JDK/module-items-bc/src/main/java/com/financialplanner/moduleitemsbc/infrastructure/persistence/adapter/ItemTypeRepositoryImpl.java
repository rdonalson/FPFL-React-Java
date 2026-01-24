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
 * Implementation of the {@code ItemTypeRepository} interface, serving as a bridge between
 * the application and the JPA-based repository for managing {@code ItemType} entities.
 * This class encapsulates the persistence logic and provides CRUD operations for
 * interacting with the {@code ItemType} data. It makes use of the {@code JpaItemTypeRepository}
 * to abstract the underlying JPA functionality and enriches it by adding domain-specific
 * exception handling and logging capabilities.
 * Responsibilities:
 * - Retrieve all or specific {@code ItemType} entities.
 * - Persist new or updated {@code ItemType} entities.
 * - Delete {@code ItemType} entities by their identifiers.
 * Exception Handling:
 * - Wraps exceptions from underlying persistence mechanisms in custom business exceptions
 * such as {@code RepositoryException}, {@code DuplicateItemException}, and {@code ItemNotFoundException}
 * to provide meaningful error messages and a standardized error handling approach.
 * Thread Safety:
 * - The class is stateless, except for its dependency on {@code JpaItemTypeRepository},
 * which is thread-safe as it is managed by Spring's dependency injection container.
 */
@Repository
public class ItemTypeRepositoryImpl implements ItemTypeRepository {

    private final JpaItemTypeRepository jpa;

    public ItemTypeRepositoryImpl(JpaItemTypeRepository jpa) {
        this.jpa = jpa;
    }

    /**
     * Retrieves all {@code ItemType} entities from the underlying data source.
     * This method interacts with the JPA repository to fetch a list of all persisted
     * {@code ItemType} entities. If an issue occurs during the database operation, a
     * {@code RepositoryException} is thrown to encapsulate the error.
     *
     * @return a list of {@code ItemType} entities representing all items available
     * in the repository. If no items are found, an empty list is returned.
     *
     * @throws RepositoryException if a database failure occurs while fetching data.
     */
    @Override
    public List<ItemType> findAll() {
        try {
            return jpa.findAll();
        } catch (DataAccessException ex) {
            throw new RepositoryException("Database failure while fetching ItemTypes", ex);
        }
    }

    /**
     * Retrieves an {@code ItemType} entity by its unique identifier.
     * This method attempts to find an {@code ItemType} entity in the underlying persistence
     * layer using the provided identifier. If the entity is found, it is returned wrapped
     * in an {@code Optional}. If not found, the returned {@code Optional} will be empty.
     * In case of a database failure or a data access exception during the operation,
     * a {@code RepositoryException} is thrown to encapsulate the error.
     *
     * @param id the unique identifier of the {@code ItemType} entity to be retrieved; must not be {@code null}.
     *
     * @return an {@code Optional} containing the found {@code ItemType} entity, or empty if no entity was found.
     *
     * @throws RepositoryException if a database failure or another data access exception occurs.
     */
    @Override
    public Optional<ItemType> findById(Long id) {
        try {
            return jpa.findById(id);
        } catch (DataAccessException ex) {
            throw new RepositoryException("Database failure while fetching ItemType " + id, ex);
        }
    }

    /**
     * Saves the provided {@code ItemType} entity to the repository. If the entity already
     * exists and violates uniqueness constraints, a {@link DuplicateItemException} is thrown.
     * If a failure occurs in the data access layer during the save operation, a
     * {@link RepositoryException} is thrown.
     *
     * @param itemType the {@code ItemType} entity to save into the repository
     *
     * @return the saved {@code ItemType} entity with any updates applied by the persistence layer
     *
     * @throws DuplicateItemException if a duplicate item exists and violates uniqueness constraints
     * @throws RepositoryException    if a database or persistence error occurs during the operation
     */
    @Override
    public ItemType save(ItemType itemType) {
        try {
            return jpa.save(itemType);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateItemException("Item already exists: " + itemType.getId(), ex);
        } catch (DataAccessException ex) {
            throw new RepositoryException("Database failure while saving ItemType " + itemType.getId(), ex);
        }
    }

    /**
     * Deletes an {@code ItemType} entity by its unique identifier.
     * If the entity does not exist, an {@code ItemNotFoundException} is thrown.
     * Handles repository-level exceptions and rethrows them as {@code RepositoryException}
     * to encapsulate underlying issues such as data integrity violations or database access errors.
     *
     * @param id the unique identifier of the {@code ItemType} entity to be deleted
     *           (must not be {@code null})
     *
     * @throws ItemNotFoundException if no {@code ItemType} entity exists with the specified {@code id}
     * @throws RepositoryException   if a data integrity or database access issue occurs during deletion
     */
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

