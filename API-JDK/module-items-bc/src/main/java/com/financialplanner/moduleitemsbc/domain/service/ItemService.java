package com.financialplanner.moduleitemsbc.domain.service;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Provides a service interface for managing items.
 *
 * This interface defines methods for common CRUD operations and
 * additional domain-specific queries related to items.
 */
public interface ItemService {
    List<Item> list();
    Item get(Long id);
    List<Item> findByUserIdAndItemTypeId(UUID userId, Long itemTypeId);
    List<Item> findByUserId(UUID userId);
    Item create(Item entity);
    Item update(Long id, Item entity);
    void delete(Long id);
}
