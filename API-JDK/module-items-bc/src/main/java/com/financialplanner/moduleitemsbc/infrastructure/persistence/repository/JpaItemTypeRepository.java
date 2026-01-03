package com.financialplanner.moduleitemsbc.infrastructure.persistence.repository;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@code ItemTypeEntity} persistence.
 * This interface extends {@code JpaRepository}, providing CRUD operations
 * and query method support for the {@code ItemTypeEntity} type.
 * -------
 * It interacts with the "item_types" table in the "fpfl" schema.
 * The entity serves as a representation of item types used to categorize or define items.
 * --------
 * Key Responsibilities:
 * - Supports standard CRUD operations out of the box.
 * - Enables the extension of custom query methods for more sophisticated use cases.
 * ----------
 * The primary key type of the entity is {@code Long}.
 */
@Repository
public interface JpaItemTypeRepository extends JpaRepository<ItemTypeEntity, Long> {
}

