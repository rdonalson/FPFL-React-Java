package com.financialplanner.moduleitemsbc.infrastructure.persistence.repository;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for performing CRUD operations on {@code ItemType} entities
 * in the persistence layer using JPA.
 *
 * This interface extends {@code JpaRepository}, providing default implementations
 * for common data access methods such as saving, finding, and deleting entities.
 * It serves as the main mechanism for interacting with the "item_types" table
 * in the "fpfl" schema.
 *
 * Features:
 * - Supports primary key type {@code Long} for entity {@code ItemType}.
 * - Leverages Spring Data JPA capabilities for query methods and repository abstraction.
 *
 * Usage:
 * This repository is typically used within service or implementation classes
 * that require access to {@code ItemType} entities. It is injected as a dependency
 * to perform persistence operations while abstracting away JPA complexities.
 *
 * Thread Safety:
 * - This interface is thread-safe when used through Spring's repository framework,
 *   as instances are managed by the container and scoped appropriately.
 */
@Repository
public interface JpaItemTypeRepository extends JpaRepository<ItemType, Long> {}

