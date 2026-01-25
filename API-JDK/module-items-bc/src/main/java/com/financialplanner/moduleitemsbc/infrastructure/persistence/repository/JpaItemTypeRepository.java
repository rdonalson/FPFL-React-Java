package com.financialplanner.moduleitemsbc.infrastructure.persistence.repository;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@code ItemType} entities.
 * This interface extends {@code JpaRepository} to provide CRUD operations
 * and additional query methods for interacting with the underlying database.
 * The {@code ItemType} entity represents item types in the financial planner system
 * and is associated with the "item_types" table in the "fpfl" schema.
 * By extending {@code JpaRepository}, this interface gains access to several
 * built-in methods such as save, findById, findAll, and deleteById.
 * These methods enable efficient interaction with the persistent data.
 */
@Repository
public interface JpaItemTypeRepository extends JpaRepository<ItemType, Long> {}

