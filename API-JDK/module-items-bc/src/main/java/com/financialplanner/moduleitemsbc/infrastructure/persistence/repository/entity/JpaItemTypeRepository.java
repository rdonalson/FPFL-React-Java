package com.financialplanner.moduleitemsbc.infrastructure.persistence.repository.entity;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@code ItemType} entities.
 * This interface extends {@code JpaRepository} to provide CRUD operations
 * and additional query methods for interacting with the underlying database.
 * The {@code ItemType} entity is associated with the "item_types" table
 * in the "fpfl" schema and represents different types of items in the
 * financial planner system.
 * By extending {@code JpaRepository}, this interface inherits built-in methods
 * such as save, findById, findAll, and deleteById, enabling efficient data
 * management and interaction with persistent storage.
 */
@Repository
public interface JpaItemTypeRepository extends JpaRepository<ItemType, Long> {}

