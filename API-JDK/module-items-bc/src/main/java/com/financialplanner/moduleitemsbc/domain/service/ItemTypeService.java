package com.financialplanner.moduleitemsbc.domain.service;

import com.financialplanner.moduleitemsbc.domain.model.ItemType;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemTypeEntity;

import java.util.List;

public interface ItemTypeService {
    ItemType create(ItemTypeEntity entity);

    ItemType update(ItemTypeEntity entity);

    List<ItemType> list();

    ItemType get(Long id);

    void delete(Long id);
}
