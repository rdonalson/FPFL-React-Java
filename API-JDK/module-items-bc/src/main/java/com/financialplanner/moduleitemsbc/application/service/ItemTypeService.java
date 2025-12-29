package com.financialplanner.moduleitemsbc.application.service;

import com.financialplanner.moduleitemsbc.domain.entity.ItemTypeEntity;
import com.financialplanner.moduleitemsbc.domain.model.ItemType;
import com.financialplanner.moduleitemsbc.domain.repository.ItemTypeRepository;
import lombok.var;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemTypeService {

    private final ItemTypeRepository repo;

    public ItemTypeService(ItemTypeRepository repo) {
        this.repo = repo;
    }

    public ItemType create(ItemTypeEntity entity) {
        return repo.save(entity);
    }

    public ItemType update(ItemTypeEntity entity) {
        var e = repo.findById(entity.getId()).orElseThrow(() -> new RuntimeException("ItemType not found"));
        return repo.save(entity);
    }

    public List<ItemType> list() {
        return repo.findAll();
    }

    public ItemType get(Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("ItemType not found"));
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}

