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
 * Implementation of the {@code ItemTypeService} interface for managing {@code ItemType} entities.
 * This service provides the business logic for interacting with {@code ItemType} entities,
 * coordinating operations with the {@code ItemTypeRepository} and ensuring that application-level
 * constraints and validation are enforced.
 * Responsibilities:
 * - Retrieve lists of all item types.
 * - Manage CRUD operations for {@code ItemType}, including validation and handling
 * for not found or duplicate entities.
 * - Enforce domain-specific rules for valid input and data integrity.
 * Exceptions:
 * - {@code DomainValidationException}: Thrown when input validation fails.
 * - {@code ItemNotFoundException}: Thrown when an {@code ItemType} entity with the specified
 * identifier cannot be found.
 * - {@code DuplicateItemException}: Thrown when attempting to create a new {@code ItemType}
 * that already exists.
 * Methods:
 * - {@code list()}: Retrieves all {@code ItemType} entities, returning an empty list if
 * no entities are found.
 * - {@code get(Long id)}: Retrieves a single {@code ItemType} by its identifier,
 * enforcing validation on the input identifier.
 * - {@code create(ItemType entity)}: Creates a new {@code ItemType}, ensuring no duplicate
 * entity exists for the given identifier.
 * - {@code update(ItemType entity)}: Updates an existing {@code ItemType}, ensuring the
 * entity exists before applying changes.
 * - {@code delete(Long id)}: Deletes an {@code ItemType} by its identifier, enforcing
 * validation on the input identifier.
 */
@Service
public class ItemTypeServiceImpl implements ItemTypeService {

    private final ItemTypeRepository repo;

    public ItemTypeServiceImpl(ItemTypeRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<ItemType> list() {
        List<ItemType> list = repo.findAll();
        return list == null ? List.of() : list;
    }

    @Override
    public ItemType get(Long id) {
        if (id == null || id <= 0) {
            throw new DomainValidationException("ItemType id must be a positive integer");
        }
        return repo.findById(id)
                   .orElseThrow(() -> new ItemNotFoundException("ItemType " + id + " not found"));
    }

    @Override
    public ItemType create(ItemType entity) {
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
        // Return the updated domain model
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

