package com.financialplanner.moduleitemsbc.domain.repository;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemType;

import java.util.List;
import java.util.Optional;

/**
 * Interface representing the repository for managing {@code ItemType} entities.
 * Provides CRUD (Create, Read, Update, Delete) operations for interacting with
 * {@code ItemType} data in the underlying data source.
 * This repository serves as an abstraction over the data persistence layer, facilitating
 * domain-driven access to {@code ItemType} entities. It encapsulates interactions
 * with the persistence framework, ensuring clean separation of concerns.
 */
public interface ItemTypeRepository {
    List<ItemType> findAll();
    Optional<ItemType> findById(Long id);
    ItemType save(ItemType entity);
    void deleteById(Long id);
}
