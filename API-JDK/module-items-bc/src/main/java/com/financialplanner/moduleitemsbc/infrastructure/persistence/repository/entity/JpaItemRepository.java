package com.financialplanner.moduleitemsbc.infrastructure.persistence.repository.entity;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.repository.custom.JpaItemRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link Item} entities.
 * This interface extends {@link JpaRepository} to provide basic CRUD operations
 * and adds custom query functionalities through {@link JpaItemRepositoryCustom}.
 * It is annotated with {@link Repository}, marking it as a Spring Data repository.
 */
@Repository
public interface JpaItemRepository extends JpaRepository<Item, Long>, JpaItemRepositoryCustom {}

