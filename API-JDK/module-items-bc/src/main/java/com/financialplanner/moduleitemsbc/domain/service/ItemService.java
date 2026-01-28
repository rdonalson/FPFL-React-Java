package com.financialplanner.moduleitemsbc.domain.service;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;

import java.util.List;

/**
 * Service interface for managing items in the domain.
 * Provides methods to perform CRUD operations on item entities.
 */
public interface ItemService {
    List<Item> list();
    Item get(Long id);
    Item create(Item entity);
    Item update(Long id, Item entity);
    void delete(Long id);
}
