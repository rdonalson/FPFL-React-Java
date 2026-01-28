package com.financialplanner.moduleitemsbc.infrastructure.persistence.repository;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for performing CRUD operations on the {@link Item} entity.
 * Extends {@link JpaRepository} to provide standard database interaction methods.
 * This interface is a Spring Data repository and should be used to interact
 * with the underlying database for {@link Item} entity persistence and retrieval.
 * It provides out-of-the-box functionality for:
 * - Creating and saving {@link Item} entities.
 * - Updating and deleting existing {@link Item} entities.
 * - Querying {@link Item} entities based on their Long primary key.
 */
@Repository
public interface JpaItemRepository extends JpaRepository<Item, Long> {}
