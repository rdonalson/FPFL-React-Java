package com.financialplanner.moduleitemsbc.domain.service;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;

import java.util.List;

public interface ItemService {
    List<Item> list();
    Item get(Long id);
    Item create(Item entity);
    Item update(Long id, Item entity);
    void delete(Long id);
}
