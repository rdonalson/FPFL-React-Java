package com.financialplanner.moduleitemsbc.application.service;

import com.financialplanner.modulecommonbc.exception.DomainValidationException;
import com.financialplanner.modulecommonbc.exception.ItemNotFoundException;
import com.financialplanner.moduleitemsbc.domain.repository.ItemRepository;
import com.financialplanner.moduleitemsbc.domain.service.ItemService;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.mapper.ItemEntityMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository repo;
    private final ItemEntityMapper mapper;

    // Constructor
    public ItemServiceImpl(ItemRepository repo, ItemEntityMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public List<Item> list() {
        // Retrieve all ItemType entities from the repository
        List<Item> list = repo.findAll();
        return list == null ? List.of() : list;
    }

    @Override
    public Item get(Long id) {
        // Validate input
        if (id == null || id <= 0) {
            throw new DomainValidationException("ItemType id must be a positive integer");
        }
        // Return the domain model
        return repo.findById(id)
                   .orElseThrow(() -> new ItemNotFoundException("Item " + id + " not found"));
    }

    @Override
    public Item create(Item entity) {
        // Return the new domain model
        return repo.save(entity);
    }

    @Override
    public Item update(Long id, Item entity) {
        // Validate input
        if (id == null || id <= 0) {
            throw new DomainValidationException("Item id must be a positive integer");
        }
        Item e = repo.findById(id)
                         .orElseThrow(() -> new ItemNotFoundException("Item " + id + " not found"));
        Item result = mapper.copyEntity(id, entity);
        // Update the domain model
        return repo.save(result);
    }

    @Override
    public void delete(Long id) {
        // Validate input
        if (id == null || id <= 0) {
            throw new DomainValidationException("Item id must be a positive integer");
        }
        // Delete the domain model
        repo.deleteById(id);
    }
}
