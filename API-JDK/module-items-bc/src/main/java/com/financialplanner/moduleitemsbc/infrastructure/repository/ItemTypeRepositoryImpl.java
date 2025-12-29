package com.financialplanner.moduleitemsbc.infrastructure.repository;

import com.financialplanner.moduleitemsbc.domain.entity.ItemTypeEntity;
import com.financialplanner.moduleitemsbc.domain.model.ItemType;
import com.financialplanner.moduleitemsbc.domain.repository.ItemTypeRepository;
import com.financialplanner.moduleitemsbc.infrastructure.mapper.ItemTypeMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ItemTypeRepositoryImpl implements ItemTypeRepository {

    private final JpaItemTypeRepository jpa;
    private final ItemTypeMapper mapper;

    public ItemTypeRepositoryImpl(JpaItemTypeRepository jpa, ItemTypeMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public ItemType save(ItemTypeEntity itemType) {
        ItemTypeEntity saved = jpa.save(itemType);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<ItemType> findById(Long id) {
        return jpa.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public List<ItemType> findAll() {
        List<ItemType> list = new ArrayList<>();
        for (ItemTypeEntity itemTypeEntity : jpa.findAll()) {
            ItemType domain = mapper.toDomain(itemTypeEntity);
            list.add(domain);
        }
        return list;
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }
}

