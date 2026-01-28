package com.financialplanner.moduleitemsbc.infrastructure.persistence.adapter;

import com.financialplanner.modulecommonbc.exception.DuplicateItemException;
import com.financialplanner.modulecommonbc.exception.ItemNotFoundException;
import com.financialplanner.modulecommonbc.exception.RepositoryException;
import com.financialplanner.moduleitemsbc.domain.repository.ItemRepository;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.repository.JpaItemRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ItemRepositoryImpl implements ItemRepository {

    private final JpaItemRepository jpa;

    // Constructor
    public ItemRepositoryImpl(JpaItemRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Item> findAll() {
        try {
            return jpa.findAll();
        } catch (DataAccessException ex) {
            throw new RepositoryException("Database failure while fetching ItemTypes", ex);
        }
    }

    @Override
    public Optional<Item> findById(Long id) {
        try {
            return jpa.findById(id);
        } catch (DataAccessException ex) {
            throw new RepositoryException("Database failure while fetching ItemType " + id, ex);
        }
    }

    @Override
    public Item save(Item entity) {
        try {
            return jpa.save(entity);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateItemException("Item already exists: " + entity.getId(), ex);
        } catch (DataAccessException ex) {
            throw new RepositoryException("Database failure while saving Item " + entity.getId(), ex);
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            if (!jpa.existsById(id)) {
                throw new ItemNotFoundException("Item " + id + " not found");
            }
            jpa.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new RepositoryException("Constraint violation while deleting Item " + id, ex);
        } catch (DataAccessException ex) {
            throw new RepositoryException("Database failure while deleting Item " + id, ex);
        }
    }
}

