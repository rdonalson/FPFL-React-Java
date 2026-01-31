package com.financialplanner.moduleitemsbc.application.service;

import com.financialplanner.modulecommonbc.exception.DomainValidationException;
import com.financialplanner.modulecommonbc.exception.ItemNotFoundException;
import com.financialplanner.moduleitemsbc.domain.repository.ItemRepository;
import com.financialplanner.moduleitemsbc.domain.service.ItemService;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.mapper.ItemEntityMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of the {@link ItemService} interface that provides business logic
 * for managing {@link Item} entities. This class is responsible for handling the
 * primary operations related to item entities, including listing, retrieving,
 * creating, updating, and deleting items, as well as querying items by specific
 * parameters.
 * Dependencies:
 * - {@link ItemRepository}: For interaction with the data source containing
 * {@link Item} entities.
 * - {@link ItemEntityMapper}: For mapping properties of {@link Item} entities
 * during updates.
 * Responsibilities:
 * - Retrieve a list of all {@link Item} entities.
 * - Retrieve a specific {@link Item} based on its unique identifier.
 * - Query items by their user ID and associated item type ID.
 * - Create and save new {@link Item} entities.
 * - Update existing {@link Item} entities based on their identifier.
 * - Delete {@link Item} entities by their unique identifier.
 * Validation:
 * - Input parameters such as IDs and entity objects are validated to ensure
 * they meet the necessary constraints. Invalid input results in exceptions
 * such as {@link DomainValidationException}.
 * Error Handling:
 * - Throws {@link ItemNotFoundException} when the requested {@link Item} entity
 * does not exist in the repository.
 */
@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository repo;
    private final ItemEntityMapper mapper;

    // Constructor
    public ItemServiceImpl(ItemRepository repo, ItemEntityMapper mapper) {
        this.repo   = repo;
        this.mapper = mapper;
    }

    @Override
    public List<Item> list() {
        // Retrieve all ItemType entities from the repository
        List<Item> list = repo.findAll();
        return list == null ? List.of() : list;
    }

    @Override
    public Item get(Long id) {
        // Validate input
        if (id == null || id <= 0) {
            throw new DomainValidationException("ItemType id must be a positive integer");
        }
        // Return the domain model
        return repo.findById(id)
                   .orElseThrow(() -> new ItemNotFoundException("Item " + id + " not found"));
    }

    @Override
    public List<Item> findByUserIdAndItemTypeId(UUID userId, Long itemTypeId) {
        // Validate input
        if (userId == null) {
            throw new DomainValidationException("UserId cannot be null");
        }
        if (itemTypeId == null || itemTypeId <= 0) {
            throw new DomainValidationException("ItemType id must be a positive integer");
        }
        // Return the domain model
        return repo.findByUserIdAndItemTypeId(userId, itemTypeId);
    }

    @Override
    public Item create(Item entity) {
        // Return the new domain model
        return repo.save(entity);
    }

    @Override
    public Item update(Long id, Item entity) {
        // Validate input
        if (id == null || id <= 0) {
            throw new DomainValidationException("Item id must be a positive integer");
        }
        Item e = repo.findById(id)
                     .orElseThrow(() -> new ItemNotFoundException("Item " + id + " not found"));
        Item result = mapper.copyEntity(id, entity);
        // Update the domain model
        return repo.save(result);
    }

    @Override
    public void delete(Long id) {
        // Validate input
        if (id == null || id <= 0) {
            throw new DomainValidationException("Item id must be a positive integer");
        }
        // Delete the domain model
        repo.deleteById(id);
    }
}
