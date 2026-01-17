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
 * Service implementation for managing {@code ItemType} entities.
 * This class provides the concrete implementation of operations defined
 * in the {@code ItemTypeService} interface and interacts with the
 * {@code ItemTypeRepository} for persisting, retrieving, updating, and
 * deleting {@code ItemType} entities.
 *
 * Responsibilities of the {@code ItemTypeServiceImpl} include:
 * - Creating new {@code ItemType} entities while ensuring no duplicate entries.
 * - Updating existing {@code ItemType} entities, ensuring their existence beforehand.
 * - Retrieving a specific {@code ItemType} by its unique identifier.
 * - Listing all available {@code ItemType} entities stored in the repository.
 * - Deleting a specific {@code ItemType} entity based on its identifier.
 *
 * Exceptions handled by this implementation:
 * - {@code DuplicateItemException} if an {@code ItemType} with the given ID already exists during creation.
 * - {@code ItemNotFoundException} if an {@code ItemType} with the specified ID does not exist in the repository.
 * - {@code InvalidRequestException} if an invalid condition is encountered, such as an ID of zero during retrieval.
 *
 * This class is annotated with {@code @Service}, making it a Spring-managed component.
 */
@Service
public class ItemTypeServiceImpl implements ItemTypeService {

    private final ItemTypeRepository repo;

    public ItemTypeServiceImpl(ItemTypeRepository repo) {
        this.repo = repo;
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
        ItemType e = repo.findById(entity.getId())
                         .orElseThrow(() -> new ItemNotFoundException("ItemType " + entity.getId() + " not found"));
        // Return the updated domain model
        return repo.save(entity);
    }

    @Override
    public List<ItemType> list() {
        return repo.findAll();
    }

    @Override
    public ItemType get(Long id) {
        if (id == 0) {
            throw new InvalidRequestException("ItemType ID cannot be zero");
        }
        return repo.findById(id)
                   .orElseThrow(() -> new ItemNotFoundException("ItemType " + id + " not found"));
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }
}

