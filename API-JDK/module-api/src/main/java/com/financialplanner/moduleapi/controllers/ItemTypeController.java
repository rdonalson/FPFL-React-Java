package com.financialplanner.moduleapi.controllers;

import com.financialplanner.moduleapi.dto.CreateItemTypeRequest;
import com.financialplanner.moduleitemsbc.application.service.ItemTypeService;
import com.financialplanner.moduleitemsbc.domain.entity.ItemTypeEntity;
import com.financialplanner.moduleitemsbc.domain.model.ItemType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/item-types")
public class ItemTypeController {

    @Autowired
    private final ItemTypeService service;

    public ItemTypeController(ItemTypeService service) {
        this.service = service;
    }

    @PostMapping
    public ItemType create(@RequestBody CreateItemTypeRequest request) {
        ItemTypeEntity entity = new ItemTypeEntity();
        entity.setId(request.getId());
        entity.setName(request.getName());
        return service.create(entity);
    }

    @GetMapping
    public List<ItemType> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public ItemType get(@PathVariable Long id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    public ItemType update(@PathVariable Long id, @RequestBody CreateItemTypeRequest request) {
        ItemTypeEntity entity = new ItemTypeEntity();
        entity.setId(request.getId());
        entity.setName(request.getName());
        return service.update(entity);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}

