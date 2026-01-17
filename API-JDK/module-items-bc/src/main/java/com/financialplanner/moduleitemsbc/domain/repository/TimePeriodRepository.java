package com.financialplanner.moduleitemsbc.domain.repository;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.TimePeriod;

import java.util.List;
import java.util.Optional;

/**
 * Interface representing the repository for managing {@code TimePeriod} entities.
 * Provides CRUD (Create, Read, Update, Delete) operations for interacting with
 * {@code TimePeriod} data within the underlying data source.
 *
 * This repository abstracts the data persistence layer, enabling domain-driven access
 * to {@code TimePeriod} entities while encapsulating the necessary data operations.
 */
public interface TimePeriodRepository {
    TimePeriod save(TimePeriod entity);
    Optional<TimePeriod> findById(Long id);
    List<TimePeriod> findAll();
    void deleteById(Long id);
}
