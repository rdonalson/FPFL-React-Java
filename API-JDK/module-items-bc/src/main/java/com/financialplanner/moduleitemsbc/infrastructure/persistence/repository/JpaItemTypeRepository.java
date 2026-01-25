package com.financialplanner.moduleitemsbc.infrastructure.persistence.repository;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for performing CRUD operations on {@code ItemType} entities.
 * Extends the {@code JpaRepository} interface provided by Spring Data JPA.
 * This repository serves as a mechanism to interact with the underlying data store
 * for {@code ItemType} entities through JPA-based methods.
 * This is annotated as a {@code @Repository} to indicate that it is a Spring bean
 * and participates in Spring's exception translation mechanism for persistence layers.
 */
@Repository
public interface JpaItemTypeRepository extends JpaRepository<ItemType, Long> {}

