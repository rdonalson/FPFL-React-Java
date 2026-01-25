package com.financialplanner.moduleitemsbc.application.service;

import com.financialplanner.modulecommonbc.exception.DomainValidationException;
import com.financialplanner.modulecommonbc.exception.DuplicateItemException;
import com.financialplanner.modulecommonbc.exception.ItemNotFoundException;
import com.financialplanner.moduleitemsbc.domain.repository.ItemTypeRepository;
import com.financialplanner.moduleitemsbc.domain.service.ItemTypeService;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemType;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of {@code ItemTypeService} for managing {@code ItemType} entities.
 * This class provides the concrete business logic to interact with the persistence layer
 * through the {@code ItemTypeRepository} and implements the operations defined in the
 * {@code ItemTypeService} interface.
 * Responsibilities:
 * - Handling the creation, retrieval, updating, listing, and deletion of {@code ItemType} entities.
 * - Enforcing input validation and application-level constraints.
 * - Ensuring proper error handling for cases such as missing or duplicate entities.
 * Methods:
 * - {@code list()}: Returns all {@code ItemType} entities from the repository.
 * - {@code get(Long id)}: Retrieves a specific {@code ItemType} by its identifier. Throws exceptions in case of
 * invalid input or if the entity does not exist.
 * - {@code create(ItemType entity)}: Creates a new {@code ItemType} entity in the repository. Throws an exception if
 * the entity already exists.
 * - {@code update(ItemType entity)}: Updates the details of an existing {@code ItemType} entity. Throws an exception
 * if the entity does not exist.
 * - {@code delete(Long id)}: Deletes a specific {@code ItemType} entity by its identifier. Throws exceptions for
 * invalid input or if the entity does not exist.
 * Exceptions:
 * - {@code DomainValidationException}: Thrown for invalid inputs or constraints violations.
 * - {@code DuplicateItemException}: Thrown when attempting to create an entity that already exists.
 * - {@code ItemNotFoundException}: Thrown when an entity cannot be found.
 */
@Service
public class ItemTypeServiceImpl implements ItemTypeService {

    private final ItemTypeRepository repo;

    // Constructor
    public ItemTypeServiceImpl(ItemTypeRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<ItemType> list() {
        // Retrieve all ItemType entities from the repository
        List<ItemType> list = repo.findAll();
        return list == null ? List.of() : list;
    }

    @Override
    public ItemType get(Long id) {
        // Validate input
        if (id == null || id <= 0) {
            throw new DomainValidationException("ItemType id must be a positive integer");
        }
        // Return the domain model
        return repo.findById(id)
                   .orElseThrow(() -> new ItemNotFoundException("ItemType " + id + " not found"));
    }

    @Override
    public ItemType create(ItemType entity) {
        // Validate input
        if (repo.findById(entity.getId())
                .isPresent()) {
            throw new DuplicateItemException("ItemType " + entity.getId() + " already exists.");
        }
        // Return the new domain model
        return repo.save(entity);
    }

    @Override
    public ItemType update(ItemType entity) {
        // Validate input
        ItemType e = repo.findById(entity.getId())
                         .orElseThrow(() -> new ItemNotFoundException("ItemType " + entity.getId() + " not found"));
        // Update the domain model
        return repo.save(entity);
    }

    @Override
    public void delete(Long id) {
        // Validate input
        if (id == null || id <= 0) {
            throw new DomainValidationException("ItemType id must be a positive integer");
        }
        // Delete the domain model
        repo.deleteById(id);
    }
}

