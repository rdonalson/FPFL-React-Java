package com.financialplanner.moduleitemsbc.infrastructure.persistence.repository.custom;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;

import java.util.List;
import java.util.UUID;

/**
 * Custom repository interface for executing complex or non-standard queries
 * related to {@link Item} entities that cannot be handled directly by standard
 * Spring Data JPA repository methods.
 */
public interface JpaItemRepositoryCustom {
    List<Item> findByUserIdAndItemTypeId(UUID userId, Long itemTypeId);
}

