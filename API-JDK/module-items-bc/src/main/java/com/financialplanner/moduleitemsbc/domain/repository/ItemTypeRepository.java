package com.financialplanner.moduleitemsbc.domain.repository;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemType;

import java.util.List;
import java.util.Optional;

/**
 * Interface representing the repository for managing {@link ItemType} instances.
 * Provides CRUD operations for persisting and retrieving item type-related data
 * from the underlying data source.
 * <p>
 * This interface defines the contract for managing ItemType entities and transforms
 * them into domain models. The abstraction enables a clean separation between
 * business logic and persistence details.
 */
public interface ItemTypeRepository {
    /**
     * Saves the provided {@link ItemType} instance and returns the corresponding
     * domain model {@link com.financialplanner.moduleitemsbc.domain.model.ItemType} after successful persistence.
     * This method persists the entity in the underlying data store and transforms
     * it into a usable domain object.
     *
     * @param entity the {@link ItemType} object to be saved; must not be null
     * @return the resulting {@link com.financialplanner.moduleitemsbc.domain.model.ItemType} object after the entity has been saved
     */
    ItemType save(ItemType entity);

    /**
     * Retrieves an optional {@link com.financialplanner.moduleitemsbc.domain.model.ItemType} instance by its unique identifier.
     * If an entity with the specified ID exists, it is returned wrapped in an {@link Optional}.
     * If no entity is found, an empty {@link Optional} is returned.
     *
     * @param id the unique identifier of the {@link com.financialplanner.moduleitemsbc.domain.model.ItemType} to retrieve; must not be null
     * @return an {@link Optional} containing the {@link com.financialplanner.moduleitemsbc.domain.model.ItemType} if found, or an empty {@link Optional} if not found
     */
    Optional<ItemType> findById(Long id);

    /**
     * Retrieves a list of all available ItemType instances from the underlying data store.
     * This method queries the repository for all item type records and returns them
     * as a collection of domain models.
     *
     * @return a list of ItemType objects representing all item types present in the data source
     */
    List<ItemType> findAll();

    /**
     * Deletes the entity with the specified unique identifier from the underlying data store.
     * This method removes the record corresponding to the given ID if it exists.
     *
     * @param id the unique identifier of the entity to be deleted; must not be null
     */
    void deleteById(Long id);
}
