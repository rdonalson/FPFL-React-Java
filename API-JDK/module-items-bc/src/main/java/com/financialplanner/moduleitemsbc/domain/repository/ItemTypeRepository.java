package com.financialplanner.moduleitemsbc.domain.repository;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemType;

import java.util.List;
import java.util.Optional;

/**
 * Interface for managing {@code ItemType} entities in the repository layer.
 * Provides standard CRUD (Create, Read, Update, Delete) operations to interact
 * with the underlying data source containing {@code ItemType} data.
 * This repository abstracts the data access layer, allowing seamless interaction
 * with {@code ItemType} entities while encapsulating the specifics of persistence.
 */
public interface ItemTypeRepository {
    List<ItemType> findAll();
    Optional<ItemType> findById(Long id);
    ItemType getReferenceById(Long id);
    ItemType save(ItemType entity);
    void deleteById(Long id);
}
