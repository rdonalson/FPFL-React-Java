package com.financialplanner.moduleitemsbc.application.service;

import com.financialplanner.modulecommonbc.exception.DuplicateItemException;
import com.financialplanner.modulecommonbc.exception.InvalidRequestException;
import com.financialplanner.modulecommonbc.exception.ItemNotFoundException;
import com.financialplanner.moduleitemsbc.domain.repository.ItemTypeRepository;
import com.financialplanner.moduleitemsbc.domain.service.ItemTypeService;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemType;
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
     * Repository instance used for performing data persistence operations
     * related to {@code ItemType} entities.
     *
     * Acts as the primary access point for interacting with the underlying data
     * layer, encapsulating CRUD operations such as saving, updating, retrieving,
     * and deleting {@code ItemType} records.
     *
     * This instance is injected into the service to ensure separation of
     * concerns between business logic and data access, promoting modularity
     * and testability of the service implementation.
     */
    private final ItemTypeRepository repo;

    /**
     * Constructs a new instance of {@code ItemTypeServiceImpl} with the specified repository.
     * This constructor initializes the service with an {@code ItemTypeRepository} that will be used
     * to perform data operations such as create, read, update, and delete for {@code ItemType} entities.
     *
     * @param repo the {@code ItemTypeRepository} to be used by this service implementation; must not be null
     */
    public ItemTypeServiceImpl(ItemTypeRepository repo) {
        this.repo = repo;
    }

    /**
     * Creates a new {@code ItemType} entity and persists it in the repository.
     * If an {@code ItemType} with the same ID already exists, a {@code DuplicateItemException} is thrown.
     *
     * @param entity the {@code ItemType} entity to be created, must not be null
     * @return the newly created and persisted {@code ItemType} entity
     * @throws DuplicateItemException if an {@code ItemType} with the same ID already exists
     */
    @Override
    public ItemType create(ItemType entity) {
        if (repo.findById(entity.getId())
                .isPresent()) {
            throw new DuplicateItemException("ItemType " + entity.getId() + " already exists.");
        }
        // Return the new domain model
        return repo.save(entity);
    }

    /**
     * Updates an existing {@code ItemType} entity in the data store.
     * The method validates that the entity exists by attempting to retrieve it
     * using its unique identifier. If the entity does not exist, an {@code ItemNotFoundException}
     * is thrown. If the entity exists, it is updated and persisted in the data store.
     *
     * @param entity the {@code ItemType} entity to be updated. Must be non-null and must contain a valid ID.
     * @return the updated {@code ItemType} entity after being successfully persisted in the data store.
     * @throws ItemNotFoundException if no {@code ItemType} matching the given ID is found in the data store.
     */
    @Override
    public ItemType update(ItemType entity) {
        ItemType e = repo.findById(entity.getId())
                         .orElseThrow(() -> new ItemNotFoundException("ItemType " + entity.getId() + " not found"));
        // Return the updated domain model
        return repo.save(entity);
    }

    /**
     * Retrieves all {@code ItemType} entities from the underlying data source.
     * This method returns a complete list of item types available in the repository.
     *
     * @return a list of {@code ItemType} entities, or an empty list if no entities are found
     */
    @Override
    public List<ItemType> list() {
        return repo.findAll();
    }

    /**
     * Retrieves an {@code ItemType} entity based on its unique identifier.
     * If the provided ID is zero, an {@code InvalidRequestException} is thrown.
     * If no {@code ItemType} is found for the given ID, an {@code ItemNotFoundException} is thrown.
     *
     * @param id the unique identifier of the {@code ItemType} to be retrieved; must not be zero
     * @return the {@code ItemType} entity associated with the provided ID
     * @throws InvalidRequestException if the provided ID is zero
     * @throws ItemNotFoundException if no {@code ItemType} entity is found for the given ID
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
     * Deletes an existing ItemType entity identified by the given ID.
     * This operation removes the entity from the underlying data store.
     *
     * @param id the unique identifier of the ItemType to be deleted; must not be null.
     *           If the specified ID does not exist in the repository, no action is performed.
     */
    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }
}

