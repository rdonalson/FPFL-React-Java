package com.financialplanner.moduleitemsbc.domain.service;

import com.financialplanner.moduleitemsbc.domain.model.ItemType;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemTypeEntity;

import java.util.List;

/**
 * Service interface for managing operations related to ItemType entities.
 * Provides methods for creating, updating, retrieving, deleting, and listing item types.
 * This interface serves as a contract for the service layer to interact with ItemType entities
 * in the underlying data store.
 */
public interface ItemTypeService {
    ItemTypeEntity create(ItemTypeEntity entity);

    ItemTypeEntity update(ItemTypeEntity entity);

    List<ItemTypeEntity> list();

    ItemTypeEntity get(Long id);

    void delete(Long id);
}
