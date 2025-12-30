package com.financialplanner.moduleapi.controllers;

import com.financialplanner.moduleapi.dto.ItemTypeDto;
import com.financialplanner.moduleapi.mapper.ItemTypeDtoMapper;
import com.financialplanner.moduleitemsbc.application.service.ItemTypeService;
import com.financialplanner.moduleitemsbc.domain.entity.ItemTypeEntity;
import com.financialplanner.moduleitemsbc.domain.model.ItemType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/item-types")
public class ItemTypeController {

    private final ItemTypeService service;
    private final ItemTypeDtoMapper mapper;

    public ItemTypeController(ItemTypeService service, ItemTypeDtoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public List<ItemType> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public ItemType get(@PathVariable("id") Long id) {
        return service.get(id);
    }

    @PostMapping
    public ItemType create(@RequestBody ItemTypeDto request) {
        ItemTypeEntity entity = mapper.toEntity(request);;
        return service.create(entity);
    }

    @PutMapping("/{id}/{name}")
    public ItemType update(@PathVariable("id") Long id, @PathVariable("name") String name) {
        ItemTypeEntity entity = new ItemTypeEntity();
        entity.setId(id);
        entity.setName(name);
        return service.update(entity);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        service.delete(id);
    }
}

