package com.financialplanner.moduleitemsbc.application.service;

import com.financialplanner.moduleitemsbc.domain.exception.DuplicateItemException;
import com.financialplanner.moduleitemsbc.domain.exception.InvalidRequestException;
import com.financialplanner.moduleitemsbc.domain.exception.ItemNotFoundException;
import com.financialplanner.moduleitemsbc.domain.model.ItemType;
import com.financialplanner.moduleitemsbc.domain.repository.ItemTypeRepository;
import com.financialplanner.moduleitemsbc.domain.service.ItemTypeService;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemTypeEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class responsible for managing operations related to ItemType entities.
 * This includes creating, updating, retrieving, and deleting item types from the underlying data store.
 * The service leverages the ItemTypeRepository to interact with the database, ensuring encapsulation
 * and separation of concerns between business logic and data access layers.
 */
@Service
public class ItemTypeServiceImpl implements ItemTypeService {

    /**
     * Repository instance responsible for performing data access operations
     * related to the {@link ItemTypeEntity}.
     * The repository provides methods for saving, retrieving, updating, and deleting
     * ItemType records in the underlying data store. It acts as a bridge between
     * the application business logic and the persistence layer, enabling effective
     * interaction with the item type data.
     */
    private final ItemTypeRepository repo;

    /**
     * Constructs a new instance of the ItemTypeService class.
     * This service is responsible for managing operations related to ItemType entities,
     * such as creating, updating, retrieving, and deleting item types.
     * It uses the provided ItemTypeRepository to interact with the underlying data store.
     *
     * @param repo the ItemTypeRepository instance used to perform database operations; must not be null
     */
    public ItemTypeServiceImpl(ItemTypeRepository repo) {
        this.repo = repo;
    }

    /**
     * Creates and persists a new ItemType by saving the provided ItemTypeEntity.
     *
     * @param entity the ItemTypeEntity object to be saved; must not be null
     * @return the saved ItemType object after successful persistence
     */
    @Override
    public ItemType create(ItemTypeEntity entity) {
        if (repo.findById(entity.getId())
                .isPresent()) {
            throw new DuplicateItemException("ItemType already exists: " + entity.getId());
        }
        // Return the new domain model
        return repo.save(entity);
    }

    /**
     * Updates an existing ItemType in the database.
     * The method first checks if the entity exists in the repository. If the entity is not found,
     * a RuntimeException is thrown. Otherwise, the entity is updated.
     *
     * @param entity the ItemTypeEntity to be updated. The entity must include a valid ID
     *               that corresponds to an existing record in the database.
     * @return the updated ItemType instance reflecting changes persisted in the database.
     * @throws RuntimeException if the entity with the provided ID does not exist in the repository.
     */
    @Override
    public ItemType update(ItemTypeEntity entity) {
        ItemType e = repo.findById(entity.getId())
                         .orElseThrow(() -> new ItemNotFoundException("ItemType not found"));
        // Return the updated domain model
        return repo.save(entity);
    }

    /**
     * Retrieves a list of all available item types from the repository.
     *
     * @return a list of ItemType objects representing all item types present in the data store
     */
    @Override
    public List<ItemType> list() {
        return repo.findAll();
    }

    /**
     * Retrieves an ItemType based on the provided unique identifier (ID).
     * If the ItemType with the given ID is not found, a RuntimeException is thrown.
     *
     * @param id the unique identifier of the ItemType to retrieve; must not be null
     * @return the ItemType associated with the given ID
     * @throws RuntimeException if no ItemType is found with the specified ID
     */
    @Override
    public ItemType get(Long id) {
        if (id == 0) {
            throw new InvalidRequestException("ItemType ID cannot be zero");
        }
        return repo.findById(id)
                   .orElseThrow(() -> new ItemNotFoundException("ItemType " + id + " not found"));
    }

    /**
     * Deletes an ItemType entity with the specified unique identifier from the repository.
     * This method leverages the underlying repository implementation to remove the entity
     * from the data store based on its ID.
     *
     * @param id the unique identifier of the ItemType entity to be deleted; must not be null.
     */
    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }
}

