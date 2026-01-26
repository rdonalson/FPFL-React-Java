package com.financialplanner.moduleitemsbc.application.service;

import com.financialplanner.modulecommonbc.exception.DomainValidationException;
import com.financialplanner.modulecommonbc.exception.DuplicateItemException;
import com.financialplanner.modulecommonbc.exception.ItemNotFoundException;
import com.financialplanner.moduleitemsbc.domain.repository.TimePeriodRepository;
import com.financialplanner.moduleitemsbc.domain.service.TimePeriodService;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.TimePeriod;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TimePeriodServiceImpl implements TimePeriodService {

    private final TimePeriodRepository repo;

    // Constructor
    public TimePeriodServiceImpl(TimePeriodRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<TimePeriod> list() {
        // Retrieve all TimePeriod entities from the repository
        List<TimePeriod> list = repo.findAll();
        return list == null ? List.of() : list;
    }

    @Override
    public TimePeriod get(Long id) {
        // Validate input
        if (id == null || id <= 0) {
            throw new DomainValidationException("TimePeriod id must be a positive integer");
        }
        // Return the domain model
        return repo.findById(id)
                   .orElseThrow(() -> new ItemNotFoundException("TimePeriod " + id + " not found"));
    }

    @Override
    public TimePeriod create(TimePeriod entity) {
        // Validate input
        if (repo.findById(entity.getId())
                .isPresent()) {
            throw new DuplicateItemException("TimePeriod " + entity.getId() + " already exists.");
        }
        // Return the new domain model
        return repo.save(entity);
    }

    @Override
    public TimePeriod update(TimePeriod entity) {
        // Validate input
        TimePeriod _ = repo.findById(entity.getId())
                           .orElseThrow(() -> new ItemNotFoundException("TimePeriod " + entity.getId() + " not found"));
        // Update the domain model
        return repo.save(entity);
    }

    @Override
    public void delete(Long id) {
        // Validate input
        if (id == null || id <= 0) {
            throw new DomainValidationException("TimePeriod id must be a positive integer");
        }
        // Delete the domain model
        repo.deleteById(id);
    }
}
