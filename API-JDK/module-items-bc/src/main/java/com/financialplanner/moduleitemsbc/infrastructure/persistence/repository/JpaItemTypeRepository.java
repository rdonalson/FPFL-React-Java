package com.financialplanner.moduleitemsbc.infrastructure.persistence.repository;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link ItemType} entities.
 * Extends the {@link JpaRepository} interface to provide basic CRUD operations
 * and query construction capabilities for the {@link ItemType} entity.
 *
 * This interface serves as a bridge between the persistence layer and the service layer
 * by leveraging Spring Data JPA's repository abstraction, enabling interaction
 * with the database without requiring the implementation of boilerplate code.
 *
 * Annotations:
 * - {@code @Repository}: Indicates that this interface is a repository that should
 *   be managed by the Spring framework as a bean for dependency injection.
 *
 * Features:
 * - Inherits generic CRUD and pagination operations from {@link JpaRepository}.
 * - Operates on {@link ItemType} entities with a primary key of type {@code Long}.
 */
@Repository
public interface JpaItemTypeRepository extends JpaRepository<ItemType, Long> {}

