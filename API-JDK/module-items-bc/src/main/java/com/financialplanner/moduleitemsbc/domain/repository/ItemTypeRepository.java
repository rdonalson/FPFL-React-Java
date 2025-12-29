package com.financialplanner.moduleitemsbc.domain.repository;

import java.util.List;
import java.util.Optional;
import com.financialplanner.moduleitemsbc.domain.entity.ItemTypeEntity;
import com.financialplanner.moduleitemsbc.domain.model.ItemType;

public interface ItemTypeRepository {
    ItemType save(ItemTypeEntity entity);
    Optional<ItemType> findById(Long id);
    List<ItemType> findAll();
    void deleteById(Long id);
}
