package com.financialplanner.moduleitemsbc.infrastructure.mapper;

import com.financialplanner.moduleitemsbc.domain.entity.ItemTypeEntity;
import com.financialplanner.moduleitemsbc.domain.model.ItemType;
import org.springframework.stereotype.Component;

@Component
public class ItemTypeMapper {
    public ItemType toDomain(ItemTypeEntity entity) {
        return new ItemType(
            entity.getId(),
            entity.getName()
        );
    }

    public ItemTypeEntity toEntity(ItemType domain) {
        ItemTypeEntity e = new ItemTypeEntity();
        e.setId(domain.getId());
        e.setName(domain.getName());
        return e;
    }
}
